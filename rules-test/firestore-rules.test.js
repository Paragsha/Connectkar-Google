const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const fs = require('fs');
const path = require('path');

describe('Firestore Security Rules: Price-Tampering Protection', function () {
  this.timeout(15000);
  let testEnv;

  before(async () => {
    testEnv = await initializeTestEnvironment({
      projectId: 'connectkar-security-test',
      firestore: {
        rules: fs.readFileSync(path.resolve(__dirname, '../firestore.rules'), 'utf8'),
        host: '127.0.0.1',
        port: 8088
      }
    });
  });

  after(async () => {
    if (testEnv) {
      await testEnv.cleanup();
    }
  });

  beforeEach(async () => {
    await testEnv.clearFirestore();

    // Seed database with verified users and authentic menu items using admin context
    await testEnv.withSecurityRulesDisabled(async (context) => {
      const db = context.firestore();
      
      // Seed buyer user profile
      await db.collection('users').doc('buyer_123').set({
        uid: 'buyer_123',
        fullName: 'Aarav Mehta',
        society: 'Palm Grove',
        isVerified: true,
        role: 'RESIDENT'
      });

      // Seed chef user profile
      await db.collection('users').doc('chef_456').set({
        uid: 'chef_456',
        fullName: 'Sunita Sharma',
        society: 'Palm Grove',
        isVerified: true,
        role: 'RESIDENT'
      });

      // Seed authentic menu item with price ₹150.0
      await db.collection('menuItems').doc('kadhi_khichdi_001').set({
        chefUid: 'chef_456',
        chefName: 'Sunita Sharma',
        dishName: 'Gujarati Kadhi Khichdi',
        price: 150.0,
        portionsAvailable: 10,
        portionsBooked: 0,
        society: 'Palm Grove',
        isSoldOut: false
      });

      // Seed admin user whose role is stored in Firestore user document
      await db.collection('users').doc('admin_user').set({
        uid: 'admin_user',
        fullName: 'Admin User',
        society: 'Palm Grove',
        isVerified: true,
        role: 'ADMIN'
      });

      // Seed an outsider resident from another society
      await db.collection('users').doc('outsider_user').set({
        uid: 'outsider_user',
        fullName: 'Outsider User',
        society: 'Different Society',
        isVerified: true,
        role: 'RESIDENT'
      });
    });
  });

  it('PROVES TAMPERED grandTotal IS REJECTED: client attempts grandTotal = ₹1.0 for a ₹170.0 order', async () => {
    const buyerDb = testEnv.authenticatedContext('buyer_123').firestore();

    const tamperedOrder = {
      buyerUid: 'buyer_123',
      buyerName: 'Aarav Mehta',
      buyerFlat: 'A-302',
      chefUid: 'chef_456',
      chefName: 'Sunita Sharma',
      menuItemId: 1,
      menuItemFirestoreId: 'kadhi_khichdi_001',
      dishName: 'Gujarati Kadhi Khichdi',
      servingSize: 1,
      deliveryWindow: '12:30 PM - 1:30 PM',
      deliveryMethod: 'DOORSTEP',
      addOns: '',
      itemTotal: 150.0,
      addOnsTotal: 0.0,
      deliveryFee: 20.0,
      grandTotal: 1.0, // VULNERABILITY EXPLOIT: Tampered from 170.0 to 1.0!
      status: 'PENDING',
      society: 'Palm Grove'
    };

    // Assert: Firestore rules MUST reject this tampered write
    await assertFails(buyerDb.collection('mealOrders').doc('order_exploit_001').set(tamperedOrder));
  });

  it('PROVES TAMPERED itemTotal IS REJECTED: client attempts itemTotal = ₹10.0 instead of ₹150.0', async () => {
    const buyerDb = testEnv.authenticatedContext('buyer_123').firestore();

    const tamperedItemOrder = {
      buyerUid: 'buyer_123',
      buyerName: 'Aarav Mehta',
      buyerFlat: 'A-302',
      chefUid: 'chef_456',
      chefName: 'Sunita Sharma',
      menuItemId: 1,
      menuItemFirestoreId: 'kadhi_khichdi_001',
      dishName: 'Gujarati Kadhi Khichdi',
      servingSize: 1,
      deliveryWindow: '12:30 PM - 1:30 PM',
      deliveryMethod: 'DOORSTEP',
      addOns: '',
      itemTotal: 10.0, // Tampered item price!
      addOnsTotal: 0.0,
      deliveryFee: 20.0,
      grandTotal: 30.0, // Consistent with tampered item, but mismatches actual menu price
      status: 'PENDING',
      society: 'Palm Grove'
    };

    await assertFails(buyerDb.collection('mealOrders').doc('order_exploit_002').set(tamperedItemOrder));
  });

  it('PROVES TAMPERED deliveryFee IS REJECTED: client chooses DOORSTEP delivery with ₹0 fee', async () => {
    const buyerDb = testEnv.authenticatedContext('buyer_123').firestore();

    const tamperedDeliveryOrder = {
      buyerUid: 'buyer_123',
      buyerName: 'Aarav Mehta',
      buyerFlat: 'A-302',
      chefUid: 'chef_456',
      chefName: 'Sunita Sharma',
      menuItemId: 1,
      menuItemFirestoreId: 'kadhi_khichdi_001',
      dishName: 'Gujarati Kadhi Khichdi',
      servingSize: 1,
      deliveryWindow: '12:30 PM - 1:30 PM',
      deliveryMethod: 'DOORSTEP',
      addOns: '',
      itemTotal: 150.0,
      addOnsTotal: 0.0,
      deliveryFee: 0.0, // Tampered delivery fee! Should be 20.0 for DOORSTEP
      grandTotal: 150.0,
      status: 'PENDING',
      society: 'Palm Grove'
    };

    await assertFails(buyerDb.collection('mealOrders').doc('order_exploit_003').set(tamperedDeliveryOrder));
  });

  it('ALLOWS VALID ORDER: legitimate order with server-verified prices and matching grandTotal', async () => {
    const buyerDb = testEnv.authenticatedContext('buyer_123').firestore();

    // 2 servings @ 150.0 = 300.0 + 40.0 (dessert) + 20.0 (doorstep delivery) = 360.0
    const legitimateOrder = {
      buyerUid: 'buyer_123',
      buyerName: 'Aarav Mehta',
      buyerFlat: 'A-302',
      chefUid: 'chef_456',
      chefName: 'Sunita Sharma',
      menuItemId: 1,
      menuItemFirestoreId: 'kadhi_khichdi_001',
      dishName: 'Gujarati Kadhi Khichdi',
      servingSize: 2,
      deliveryWindow: '12:30 PM - 1:30 PM',
      deliveryMethod: 'DOORSTEP',
      addOns: 'Dessert (₹40)',
      itemTotal: 300.0,
      addOnsTotal: 40.0,
      deliveryFee: 20.0,
      grandTotal: 360.0,
      status: 'PENDING',
      society: 'Palm Grove'
    };

    // Assert: Legitimate order succeeds!
    await assertSucceeds(buyerDb.collection('mealOrders').doc('order_legit_001').set(legitimateOrder));
  });

  it('ALLOWS VALID SELF-PICKUP ORDER: 1 serving @ 150.0 + 0 delivery fee = 150.0', async () => {
    const buyerDb = testEnv.authenticatedContext('buyer_123').firestore();

    const legitimatePickupOrder = {
      buyerUid: 'buyer_123',
      buyerName: 'Aarav Mehta',
      buyerFlat: 'A-302',
      chefUid: 'chef_456',
      chefName: 'Sunita Sharma',
      menuItemId: 1,
      menuItemFirestoreId: 'kadhi_khichdi_001',
      dishName: 'Gujarati Kadhi Khichdi',
      servingSize: 1,
      deliveryWindow: '12:30 PM - 1:30 PM',
      deliveryMethod: 'Self-Pickup',
      addOns: '',
      itemTotal: 150.0,
      addOnsTotal: 0.0,
      deliveryFee: 0.0,
      grandTotal: 150.0,
      status: 'PENDING',
      society: 'Palm Grove'
    };

    await assertSucceeds(buyerDb.collection('mealOrders').doc('order_legit_002').set(legitimatePickupOrder));
  });

  it('ALLOWS ADMIN ACCESS TO chefProfiles AND menuItems VIA isAdmin() FIRESTORE USER DOC ROLE', async () => {
    // Authenticated as admin_user (role is stored in Firestore users/admin_user, without token claim)
    const adminDb = testEnv.authenticatedContext('admin_user').firestore();

    // Admin can create a chef profile for another user
    await assertSucceeds(adminDb.collection('chefProfiles').doc('chef_456').set({
      chefUid: 'chef_456',
      chefName: 'Sunita Sharma',
      society: 'Palm Grove',
      bio: 'Authentic Gujarati and North Indian cuisine',
      specialties: 'Kadhi Khichdi, Dhokla'
    }));

    // Admin can read any chef profile
    await assertSucceeds(adminDb.collection('chefProfiles').doc('chef_456').get());

    // Admin can create a menu item for any chef
    await assertSucceeds(adminDb.collection('menuItems').doc('admin_created_item_001').set({
      chefUid: 'chef_456',
      chefName: 'Sunita Sharma',
      dishName: 'Methi Thepla Combo',
      price: 120.0,
      portionsAvailable: 15,
      portionsBooked: 0,
      society: 'Palm Grove',
      isSoldOut: false
    }));

    // Admin can update a menu item
    await assertSucceeds(adminDb.collection('menuItems').doc('admin_created_item_001').update({
      portionsAvailable: 20
    }));

    // Admin can delete a menu item
    await assertSucceeds(adminDb.collection('menuItems').doc('admin_created_item_001').delete());

    // Admin can delete a chef profile
    await assertSucceeds(adminDb.collection('chefProfiles').doc('chef_456').delete());
  });

  it('REJECTS NON-ADMIN NON-AUTHOR FROM MODIFYING OR DELETING chefProfiles AND menuItems', async () => {
    const outsiderDb = testEnv.authenticatedContext('outsider_user').firestore();

    // Outsider cannot create a chef profile for another chef
    await assertFails(outsiderDb.collection('chefProfiles').doc('chef_456').set({
      chefUid: 'chef_456',
      chefName: 'Sunita Sharma',
      society: 'Palm Grove'
    }));

    // Outsider cannot delete authentic menu item of another chef
    await assertFails(outsiderDb.collection('menuItems').doc('kadhi_khichdi_001').delete());
  });
});
