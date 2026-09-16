const functions = require("firebase-functions");
const admin = require("firebase-admin");

if (admin.apps.length === 0) {
  admin.initializeApp();
}

const ADD_ON_PRICES = {
  phulkas: 20,
  phulka: 20,
  dessert: 40,
  raita: 25
};

/**
 * Calculates expected add-ons cost based on the addOns string / array
 */
function calculateExpectedAddOnsTotal(addOns) {
  if (!addOns) return 0;
  let total = 0;
  const str = (typeof addOns === "string" ? addOns : JSON.stringify(addOns)).toLowerCase();
  if (str.includes("phulka")) total += ADD_ON_PRICES.phulkas;
  if (str.includes("dessert")) total += ADD_ON_PRICES.dessert;
  if (str.includes("raita")) total += ADD_ON_PRICES.raita;
  return total;
}

/**
 * Calculates expected delivery fee
 */
function calculateExpectedDeliveryFee(deliveryMethod) {
  const method = String(deliveryMethod || "").toUpperCase();
  if (method === "DOORSTEP" || method === "DOORSTEP DELIVERY" || method === "SOCIETY_RUNNER") {
    return 20.0;
  }
  return 0.0;
}

/**
 * Triggered on creation of a mealOrder.
 * Re-derives price from menuItem doc and rejects/corrects any mismatched or tampered order.
 */
exports.validateMealOrderOnCreate = functions.firestore
  .document("mealOrders/{orderId}")
  .onCreate(async (snap, context) => {
    const orderId = context.params.orderId;
    const order = snap.data();
    if (!order) return null;

    // Skip if already server verified or if source is verified server
    if (order.priceVerified === true) {
      return null;
    }

    const menuItemDocId = order.menuItemFirestoreId || order.menuItemDocId || (order.menuItemId ? String(order.menuItemId) : null);
    if (!menuItemDocId) {
      console.error(`Order ${orderId} missing menuItem reference`);
      return snap.ref.update({
        status: "REJECTED",
        rejectionReason: "MISSING_MENU_ITEM_REFERENCE",
        tampered: true,
        verified: false,
        serverVerifiedAt: admin.firestore.FieldValue.serverTimestamp()
      });
    }

    try {
      const menuItemSnap = await admin.firestore().collection("menuItems").doc(menuItemDocId).get();
      if (!menuItemSnap.exists) {
        console.error(`Order ${orderId} references non-existent menuItem: ${menuItemDocId}`);
        return snap.ref.update({
          status: "REJECTED",
          rejectionReason: "MENU_ITEM_NOT_FOUND",
          tampered: true,
          verified: false,
          serverVerifiedAt: admin.firestore.FieldValue.serverTimestamp()
        });
      }

      const menuItem = menuItemSnap.data() || {};
      const authenticPrice = Number(menuItem.price) || 0;
      const servingSize = Number(order.servingSize) || 1;
      const expectedItemTotal = authenticPrice * servingSize;

      // Re-derive add-ons and delivery fee
      const expectedAddOnsTotal = calculateExpectedAddOnsTotal(order.addOns);
      const expectedDeliveryFee = calculateExpectedDeliveryFee(order.deliveryMethod);
      const expectedGrandTotal = expectedItemTotal + expectedAddOnsTotal + expectedDeliveryFee;

      const submittedItemTotal = Number(order.itemTotal) || 0;
      const submittedGrandTotal = Number(order.grandTotal) || 0;

      // Check if price was tampered (tolerance of 0.01 for floating-point)
      const itemTotalMismatch = Math.abs(submittedItemTotal - expectedItemTotal) > 0.01;
      const grandTotalMismatch = Math.abs(submittedGrandTotal - expectedGrandTotal) > 0.01;

      if (itemTotalMismatch || grandTotalMismatch) {
        console.warn(`[SECURITY ALERT] Price tampering detected on order ${orderId}: ` +
          `submitted grandTotal ₹${submittedGrandTotal} (expected ₹${expectedGrandTotal}), ` +
          `submitted itemTotal ₹${submittedItemTotal} (expected ₹${expectedItemTotal})`);

        return snap.ref.update({
          status: "REJECTED",
          tampered: true,
          priceVerified: false,
          rejectionReason: `PRICE_TAMPERING_DETECTED: Submitted total (₹${submittedGrandTotal}) did not match verified server total (₹${expectedGrandTotal})`,
          correctedItemTotal: expectedItemTotal,
          correctedAddOnsTotal: expectedAddOnsTotal,
          correctedDeliveryFee: expectedDeliveryFee,
          correctedGrandTotal: expectedGrandTotal,
          serverVerifiedAt: admin.firestore.FieldValue.serverTimestamp()
        });
      }

      // Valid order
      return snap.ref.update({
        priceVerified: true,
        tampered: false,
        verifiedItemTotal: expectedItemTotal,
        verifiedGrandTotal: expectedGrandTotal,
        serverVerifiedAt: admin.firestore.FieldValue.serverTimestamp()
      });
    } catch (err) {
      console.error(`Error validating order ${orderId}:`, err);
      return null;
    }
  });

/**
 * Callable function invoked at checkout to securely create an order with server-calculated prices.
 */
exports.createVerifiedMealOrder = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "User must be logged in to place an order.");
  }

  const {
    menuItemDocId,
    servingSize = 1,
    deliveryWindow,
    dietaryNotes = "",
    deliveryMethod = "DOORSTEP",
    addOns = "",
    society,
    buyerName = "",
    buyerFlat = ""
  } = data;

  if (!menuItemDocId) {
    throw new functions.https.HttpsError("invalid-argument", "menuItemDocId is required");
  }

  const menuItemSnap = await admin.firestore().collection("menuItems").doc(menuItemDocId).get();
  if (!menuItemSnap.exists) {
    throw new functions.https.HttpsError("not-found", "Menu item not found");
  }

  const menuItem = menuItemSnap.data() || {};
  if (menuItem.isSoldOut) {
    throw new functions.https.HttpsError("failed-precondition", "Menu item is sold out");
  }

  const price = Number(menuItem.price) || 0;
  const portions = Math.max(1, parseInt(servingSize, 10) || 1);
  const itemTotal = price * portions;
  const addOnsTotal = calculateExpectedAddOnsTotal(addOns);
  const deliveryFee = calculateExpectedDeliveryFee(deliveryMethod);
  const grandTotal = itemTotal + addOnsTotal + deliveryFee;

  const orderData = {
    buyerUid: context.auth.uid,
    buyerName,
    buyerFlat,
    chefUid: menuItem.chefUid || "",
    chefName: menuItem.chefName || "",
    menuItemId: menuItem.localId || 0,
    menuItemDocId,
    menuItemFirestoreId: menuItemDocId,
    dishName: menuItem.dishName || "",
    servingSize: portions,
    deliveryWindow: deliveryWindow || menuItem.deliveryWindow || "12:30 PM - 1:30 PM",
    dietaryNotes,
    deliveryMethod,
    addOns,
    itemTotal,
    addOnsTotal,
    deliveryFee,
    grandTotal,
    status: "PENDING",
    society: society || menuItem.society || "",
    priceVerified: true,
    tampered: false,
    source: "server",
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    serverTimestamp: admin.firestore.FieldValue.serverTimestamp()
  };

  const orderRef = await admin.firestore().collection("mealOrders").add(orderData);
  return {
    orderId: orderRef.id,
    grandTotal,
    status: "PENDING"
  };
});

/**
 * Triggered on creation of a mealSubscription.
 * Re-validates pricing and terms server-side.
 */
exports.validateMealSubscriptionOnCreate = functions.firestore
  .document("mealSubscriptions/{subId}")
  .onCreate(async (snap, context) => {
    const subId = context.params.subId;
    const sub = snap.data();
    if (!sub) return null;

    const pricePerMeal = Number(sub.pricePerMeal) || 0;
    const mealsPerCycle = Number(sub.mealsPerCycle) || 0;
    const discountPercent = Number(sub.discountPercent) || 0;

    if (pricePerMeal <= 0 || mealsPerCycle <= 0 || discountPercent < 0 || discountPercent > 100) {
      console.warn(`Subscription ${subId} has invalid or tampered pricing parameters`);
      return snap.ref.update({
        status: "REJECTED",
        rejectionReason: "INVALID_SUBSCRIPTION_PRICING",
        tampered: true,
        verified: false,
        serverVerifiedAt: admin.firestore.FieldValue.serverTimestamp()
      });
    }

    return snap.ref.update({
      priceVerified: true,
      tampered: false,
      serverVerifiedAt: admin.firestore.FieldValue.serverTimestamp()
    });
  });
