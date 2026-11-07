USE ucop_db;
GO

DECLARE @hash VARCHAR(255) = '$2a$10$mskd3l7WnYHH9fPoS2S6uuFIYwhIhJbce4bGRlc/tdVN4E5qdzocS';

INSERT INTO [ROLE](RoleName)
SELECT v.RoleName
FROM (VALUES ('ADMIN'), ('STAFF'), ('CUSTOMER')) v(RoleName)
WHERE NOT EXISTS (SELECT 1 FROM [ROLE] r WHERE r.RoleName = v.RoleName);

UPDATE ACCOUNT SET PasswordHash = @hash, Status = 'ACTIVE'
WHERE Username IN ('admin', 'staff', 'customer');

INSERT INTO ACCOUNT(Username, PasswordHash, Status, RoleID)
SELECT 'staff', @hash, 'ACTIVE', RoleID
FROM [ROLE] WHERE RoleName = 'STAFF'
  AND NOT EXISTS (SELECT 1 FROM ACCOUNT WHERE Username = 'staff');

INSERT INTO ACCOUNT(Username, PasswordHash, Status, RoleID)
SELECT 'customer', @hash, 'ACTIVE', RoleID
FROM [ROLE] WHERE RoleName = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM ACCOUNT WHERE Username = 'customer');

INSERT INTO ACCOUNT_PROFILE(AccountID, FullName, Email)
SELECT a.AccountID,
       CASE a.Username WHEN 'admin' THEN N'Quản trị viên'
                       WHEN 'staff' THEN N'Nhân viên kho'
                       ELSE N'Khách hàng mẫu' END,
       a.Username + '@ucop.local'
FROM ACCOUNT a
WHERE a.Username IN ('admin', 'staff', 'customer')
  AND NOT EXISTS (SELECT 1 FROM ACCOUNT_PROFILE p WHERE p.AccountID = a.AccountID);
GO
