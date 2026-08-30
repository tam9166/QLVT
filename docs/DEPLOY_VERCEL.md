# Deploy QLVT len Vercel

QLVT la ung dung Spring Boot server-side dung Thymeleaf va SQL Server, vi vay khong deploy nhu web tinh. Cach phu hop tren Vercel la deploy bang container tu `Dockerfile.vercel` va route traffic bang `vercel.json`.

## Dieu kien truoc khi deploy

- Source da duoc day len GitHub/GitLab/Bitbucket.
- Vercel project duoc import tu repository nay.
- SQL Server phai truy cap duoc tu internet hoac tu network ma Vercel co the ket noi. `localhost` tren may ca nhan khong dung duoc khi app chay tren Vercel.
- Database production da co schema QLVT. Neu dung `spring.jpa.hibernate.ddl-auto=validate`, hay chay script trong `database/` truoc khi deploy.

## Bien moi truong tren Vercel

Dat cac bien sau trong Project Settings -> Environment Variables:

```text
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:sqlserver://<host>:1433;databaseName=QLVT;encrypt=true;trustServerCertificate=true
DB_USERNAME=<sql-user>
DB_PASSWORD=<sql-password>
```

Khong dat `DB_URL` tro ve `localhost`, vi `localhost` luc do la container tren Vercel, khong phai SQL Server tren may cua ban.

## Build

Vercel se build container tu `Dockerfile.vercel`. File `vercel.json` route tat ca request `/(.*)` vao service `qlvt`, tranh tinh trang deploy thanh static site va bi 404.

Dockerfile nay:

- dung Java 17 dung voi `pom.xml`;
- build jar bang Maven;
- chay profile `prod`;
- doc cong tu bien `PORT` do nen tang deploy cap.

## Lenh CLI tuy chon

Neu muon deploy bang CLI tren may ca nhan:

```powershell
npm i -g vercel
cd E:\QLVT
vercel login
vercel link
vercel env add SPRING_PROFILES_ACTIVE production
vercel env add DB_URL production
vercel env add DB_USERNAME production
vercel env add DB_PASSWORD production
vercel deploy --prod
```

## Kiem tra sau deploy

- Mo URL production cua Vercel.
- Vao `/login`.
- Neu loi ket noi database, kiem tra lai `DB_URL`, firewall SQL Server, user/password va quyen truy cap database.
- Neu loi schema, chay lai script schema/migration tren database production hoac doi tam `spring.jpa.hibernate.ddl-auto=update` trong `application-prod.properties` cho lan khoi tao dau tien, sau do dua ve `validate`.
