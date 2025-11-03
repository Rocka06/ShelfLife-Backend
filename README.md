# ShelfLife

The team: Sohonyai Tibor, Remete Bence Dávid

Purpose:
To help people manage what they have at home, what they need to buy, and reduce waste production by notifying users what products will expire.

Functionality:
- Creating storages
  - As the owner invite other users
  - As the owner remove other users

- Adding products to a storage
  - Add by reading a barcode
  - Add by filling out input fields
- Creating new products to add to the Products table
- List out what is in the storage with an expiration date notification
- ADMIN ROLE
  - An admin can manage the users and storages on the site
- EXTRA
  - Automatically generated shopping list
  - Recipe recommendations with the products you have in your storage
  - Email notifications
  - Product filtering by type
  - Product images
  - When should a product send notifictaions about expiration or running low

- Tables:
    - Users
        - ID – auto increment
        - email
        - username – max 30 char
        - password - hashed
        - role – admin / user
    - Storages
        - ID – auto increment
        - ownerID – linked to Users
        - name – string
    - Storage
        - StorageID – linked to storages
        - UserID – linked to users
    - Products
        - ID – auto increment
        - name – string
        - barcode - number
        - ExpirationDelta – Datetime
    - StorageData
        - ID – auto increment
        - StorageID – linked to Storages
        - ProductID – linked to products
        - CreatedAt – Date
