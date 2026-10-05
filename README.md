# Library Management API

**Powered and developed by: Ibrahim Khamiss**
**WhatsApp: +201014778296**

REST API لإدارة نظام مكتبة، بصلاحيات مستخدمين (ADMIN / CLIENT) ومصادقة JWT كاملة.

---

## المرحلة الحالية

المشروع بيغطي دلوقتي:
- **Users & Authentication** — تسجيل، دخول، JWT، أدوار (ADMIN/CLIENT)
- **Books** — كتالوج الكتب (قراءة عامة، كتابة للأدمن بس)
- **Inventory** — مخزون كل كتاب (علاقة 1:1)
- **Borrowing** — استعارة وإرجاع ذاتي خدمة، بحد أقصى 3 كتب في نفس الوقت، وغرامة تأخير تلقائية (50 جنيه/يوم)
- **Orders** — شراء ذاتي خدمة (عربة تسوق بأكتر من كتاب)، مع حساب السعر الإجمالي وخصم المخزون تلقائيًا
- **Categories** — تصنيف الكتب (قراءة عامة، كتابة للأدمن بس)، كل كتاب ينتمي لفئة واحدة اختياريًا
- **Reviews** — تقييمات الكتب (1-5)، بقاعدة Verified Purchase/Borrow: العميل لازم يكون اشترى أو استعار الكتاب قبل ما يقيّمه، ومراجعة واحدة بس لكل (عميل، كتاب)
- **Reservations** — قائمة انتظار للكتب اللي خلصت من المخزون، بمزامنة تلقائية: لما المخزون يترجع، أقدم حجز يتحول لـ FULFILLED تلقائيًا (أول حد حجز بيتقدّم الأول)
- **Activity Summary** — Endpoint إحصائي مجمّع (`GET /api/users/me/activity-summary`) بيلمّ بيانات من كل الموارد: عدد الأوردرات وإجمالي الإنفاق، الاستعارات والنشطة منها، إجمالي الغرامات، عدد التقييمات، والحجوزات

كل الموارد المخطط لها اتعملت بالكامل. المشروع جاهز للتجربة المحلية والنشر.

---

## التشغيل محليًا

```bash
mvn spring-boot:run
```

السيرفر هيشتغل على `http://localhost:8080`، متصل تلقائيًا بقاعدة بيانات H2 محلية (`./data/librarydb`).

## أول أدمن (Seed تلقائي)

أول ما التطبيق يشتغل، هيتعمل أدمن تلقائي لو مفيش أي أدمن في قاعدة البيانات:

```
Email:    admin@library.com
Password: Admin@12345
```

**غيّر الباسورد ده أو اعمل أدمن جديد فورًا بعد أول تشغيل في بيئة حقيقية.**

---

## التوثيق التفاعلي (Swagger)

```
http://localhost:8080/swagger-ui.html
```

فيه زرار **Authorize** فوق — حط فيه التوكن اللي هترجعه من `/api/auth/login` (من غير كلمة `Bearer` قبله، Swagger بيضيفها لوحده) عشان تقدر تجرب الـ Endpoints المحمية.

---

## أمثلة استخدام سريعة

### 1. تسجيل عميل جديد
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Ahmed Ali","email":"ahmed@example.com","password":"123456"}'
```

### 2. تسجيل الدخول كأدمن
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@library.com","password":"Admin@12345"}'
```

### 3. إضافة كتاب (محتاج توكن أدمن)
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -d '{"title":"1984","author":"George Orwell","publicationYear":1949,"publisher":"Secker & Warburg","genre":"Fiction","price":150,"description":"A dystopian novel"}'
```

### 4. عرض الكتب (عامة، مفيش داعي لتوكن)
```bash
curl http://localhost:8080/api/books
```

---

## بنية المشروع

```
src/main/java/com/example/library/
├── LibraryApplication.java
├── config/          # SecurityConfig, AdminSeeder, OpenApiConfig
├── security/        # JwtService, JwtAuthenticationFilter
├── entity/          # User, Role, Book, Inventory
├── dto/             # طلبات وردود الـ API
├── repository/
├── service/
├── controller/
└── exception/       # معالجة أخطاء موحدة
```
