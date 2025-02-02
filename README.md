# **ENROLIUM**

## **1. Summary**

ENROLIUM was born out of the frustration students faced during chaotic course enrollment periods, where outdated systems often left learners scrambling for sections or stuck with incompatible schedules.
This project reimagines enrollment as a collaborative, transparent process rather than a zero-sum race. At its core, ENROLIUM ensures fairness through randomized lotteries during high-demand registration windows while empowering students with tools like *real-time trade matching*—a graph-based system that lets users swap sections like puzzle pieces until everyone’s schedule clicks into place.
Beyond individual needs, it acknowledges the reality of group learning: students can team up to coordinate schedules, though the system deliberately limits group advantages to prevent monopolization.
The clean JavaFX interface hides sophisticated backend mechanics—atomic seat reservations, session-aware RPC calls, and ACID-compliant transactions—behind a deliberately minimalist design, prioritizing reliability over flashy visuals.

## **2. System Overview**

### **2.1 Architecture Diagram**

![High-Level Architecture](path/to/architecture.png)
*(Annotate components: UI, RPC, DB, etc.)*

### **2.2 Technology Stack**

| Layer         | Technology    | Purpose                          |
|---------------|---------------|----------------------------------|
| Frontend      | JavaFX 17     | User interface rendering         |
| Communication | gRPC/gSocket  | Bidirectional RPC implementation |
| Database      | PostgreSQL 16.6 | Data persistence                 |

## **3. Feature Documentation**

*Repeat for each feature including the database and RPC system*

### **3.X [Feature Name]**

**Implementation Status**:
✅ Complete / 🟡 Partial (70%) / ❌ Prototype

**Demo**:
![Feature Demo GIF](path/to/demo.gif)
*(Or: [Video Link] with timestamps for key actions)*

#### **What It Does**

- Core functionality (e.g., "Real-time inventory updates")
- User-facing impact (e.g., "Reduces checkout time by 40%")

#### **How It Works**
1. **Workflow**:
    - Step 1: User triggers [Action] in `MainUI.java`
    - Step 2: RPC call via `RpcService.sendRequest()`
    - Step 3: Database transaction in `InventoryDAO.updateStock()`

3. **Patterns/Concepts**:
    - Observer pattern for UI updates
    - Connection pooling for DB access

#### **Why It Exists**

- Business need: "Required for compliance with [Regulation X]"
- Technical rationale: "Replaces legacy system bottleneck"

#### **Future Improvements**

- **Performance**: "Replace ArrayList with HashMap for O(1) lookups"
- **Functionality**: "Add bulk edit support (partially implemented in `BulkEditBranch`)"
- **UX**: "Tooltips for form fields shown in [Fig 3.X]"

### **Database System**

**Implementation Status**: ✅ Complete

**Demo**:

#### **What It Does**

- Single source of truth.
- Enforces ACID-compliant transactions for all critical operations.
- Supports complex relationships (e.g., prerequisites, trimester phases, section-teacher assignments).
- Generates demo data.

#### **Schema Design**

##### **Core Tables**
| Table                  | Description                                                                 | Relationships & Constraints                                                                                                                                                                                                 |
|------------------------|-----------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`users`**            | Base user entity (login, name, password).                                   | - Inherited by `students`/`faculty` via `user_id` (joined-table). <br> - `email` (UNIQUE, NOT NULL).                                                                                                                        |
| **`students`**         | Student-specific data (e.g., university ID).                               | - `user_id` (PK, FK to `users.id`) <br> - `university_id` (UNIQUE, NOT NULL).                                                                                                                                               |
| **`faculty`**          | Faculty members (e.g., shortcode).                                         | - `user_id` (PK, FK to `users.id`) <br> - `shortcode` (UNIQUE, NOT NULL).                                                                                                                                                   |
| **`subjects`**         | Academic courses (e.g., credits, type).                                    | - `code_name` (UNIQUE, NOT NULL) <br> - `credits` (1–5) <br> - `type` (THEORY/LAB).                                                                                                                                         |
| **`trimesters`**       | Academic periods (e.g., status, dates).                                    | - `code` (UNIQUE, format `YY[1|2|3]`) <br> - `status` enforces phase logic (e.g., `COURSE_SELECTION` requires valid dates).                                                                                                 |
| **`sections`**         | Class sections (e.g., capacity, time slots).                               | - `trimester_id` (FK to `trimesters.id`) <br> - `subject_id` (FK to `subjects.id`) <br> - `max_capacity` (≥1) <br> - `space_time_slots` (NOT EMPTY).                                                                        |
| **`courses`**          | Student enrollments (e.g., status, grade).                                 | - Unique constraint: `(student_id, subject_id, trimester_id, section_id)` <br> - `grade` (0.0–4.0) <br> - `status` validates `section`/`grade` compatibility.                                                              |
| **`prerequisites`**    | Subject dependencies (e.g., "CSE3521 requires CSE1116").                   | - `subject_id` (FK to `subjects.id`) <br> - `prerequisite_id` (FK to `subjects.id`) <br> - No cycles (code-enforced).                                                                                                       |
| **`space_time`**       | Physical/time slots for sections.                                          | - Unique constraint: `(room_number, day_of_week, timeslot)` <br> - `time_slot` validated by `room_type`.                                                                                                                   |
| **`notifications`**    | System announcements (e.g., scope, category).                              | - `scope` determines `trimester`/`section`/`target_user` validity (e.g., `SECTION` requires non-null `section_id`).                                                                                                        |

##### **Join Tables**
| Table                  | Purpose                                      | Structure                                                                                   |
|------------------------|----------------------------------------------|---------------------------------------------------------------------------------------------|
| **`faculty_subjects`** | Faculty’s teachable subjects.               | `faculty_id` (FK to `faculty.user_id`), `subject_id` (FK to `subjects.id`).                 |
| **`section_space_times`** | Assigns time slots to sections.         | `section_id` (FK to `sections.id`), `space_time_id` (FK to `space_time.id`).                |
| **`section_faculty`**  | Assigns teachers to sections.               | `section_id` (FK to `sections.id`), `faculty_id` (FK to `faculty.user_id`).                 |

The `DB` class provides reactive CRUD operations via Hibernate and RxJava. Below are common use cases:

#### **1. Saving an Entity**

```java
// Create a new student
Student student = new Student();
student.setName("John Doe");
student.setEmail("john@uiu.ac.bd");
student.setPassword("securePass123");
student.setUniversityId(112233445);

// Persist to DB
DB.save(student)
  .subscribe(
      savedStudent -> System.out.println("Saved: " + savedStudent.getId()),
      error -> System.err.println("Error: " + error.getMessage())
  );
```

#### **2. Querying Entities**

```java
// Fetch first 10 courses, sorted by creation date
DB.read(Student.class, limit, offset)
    .timeout(20, TimeUnit.SECONDS)
    .collect(ArrayList::new, (list, item) -> list.add(item))
    .onErrorResumeNext(error -> new RuntimeException("Failed to fetch student list: " + error.getMessage()));
```

#### **3. Updating an Entity**

```java
// Update a student's email
DB.findById(Student.class, UUID.fromString(id))
 .toSingle()
 .flatMap(student -> {
     student.setName(name);
     return DB.update(student);
 })
 .onErrorResumeNext(error -> new RuntimeException("Failed to update student: " + error.getMessage()));
```

#### **4. Deleting an Entity**

```java
// Delete a notification by ID
DB.delete(Student.class, UUID.fromString(id))
  .subscribe(
      () -> System.out.println("Deleted successfully"),
      error -> System.err.println("Deletion failed: " + error.getMessage())
  );
```

#### **5. Complex Queries**

```java
// Count registered courses for a student
DB.count(Course.class)
  .map(total -> "Total courses: " + total)
  .subscribe(System.out::println);

// Check if a section is full
DB.findById(Section.class, sectionId)
  .map(section -> section.getCurrentCapacity() >= section.getMaxCapacity())
  .subscribe(isFull -> System.out.println("Section full? " + isFull));
```

#### **6. Resetting the Database**

```java
// Wipe and re-seed with demo data
DB.resetAndSeed()
  .subscribe(
      () -> System.out.println("Database reset complete"),
      error -> System.err.println("Reset failed: " + error.getMessage())
  );
```

#### **7. Transactional Operations**

For custom transactions (e.g., batch updates):
```java
DB.exec(session -> {
    // Manual Hibernate operations
    session.createMutationQuery("UPDATE Course SET grade = 4.0 WHERE status = 'COMPLETED'").executeUpdate();
    return null;
}, "Batch Grade Update").subscribe();
```

#### **Future Improvements**

- Cache frequently accessed entities like `subjects` and `trimesters`.
- Implement database-level triggers for cross-table validations (e.g., section capacity checks).

### **Bidirectional RPC System**

**Implementation Status**: 🟡 Complete

#### **What It Does**

- Enables real-time client-server communication with session-aware routing
- Handles 150+ concurrent requests with automatic retries and timeouts
- Provides atomic message delivery with JSON serialization
- Supports server-initiated notifications (e.g., schedule changes)

#### **How It Works**


1. **Protocol**:
   - Message framing: 4-byte length header + JSON payload
   - Type hierarchy: `Message` → `Request`/`Response`
   - Session binding via `sessionToken` field

3. **Sequence**:
   1. Client establishes TCP connection
   2. Authentication handshake via `auth` method
   3. Subsequent requests use session token
   4. Server pushes notifications via same channel


**Code Snippet**:
```java
// Client-side
ObjectNode authParams  = JsonUtils.createObject().put("email", email).put("password", password);
Request    authRequest = Request.create(messageIdCounter.getAndIncrement(), "auth", authParams, null);
Response authResponse = ClientRPC.getInstance().sendRequest(authRequest).timeout(5, TimeUnit.SECONDS).blockingGet();
```
```java
// Server handler registration
server.registerMethod("auth", (params, request) -> {
    try {
        // Validate request parameters
        if (params == null) return Single.error(new IllegalArgumentException("Missing auth parameters"));

        String email    = JsonUtils.getString(params, "email");
        String password = JsonUtils.getString(params, "password");

        if (!(email != null && password != null && !email.trim().isEmpty() && !password.trim().isEmpty()))
            return Single.error(new IllegalArgumentException("Invalid credentials"));

        int size = Math.toIntExact(DB.count(User.class).blockingGet());

        return DB.read(User.class, size, 0)
                 .filter(user -> email.equals(user.getEmail()))
                 .firstElement()
                 .toSingle()
                 .flatMap(user -> {
                     if (!user.verifyPassword(password)) {
                         return Single.error(new IllegalArgumentException("Invalid password"));
                     }

                     // Create session
                     String UUID = user.getId().toString();
                     SessionInfo session = SessionManager.getInstance().createSession(UUID, request.getConnection().getSocket(), server);

                     // Return success response
                     JsonNode response = JsonUtils.createObject()
                                                  .put("sessionToken", session.getSessionToken())
                                                  .put("uuid", UUID)
                                                  .put("userType", user.getType().toString());

                     return Single.just(response);
                 })
                 .onErrorResumeNext(error -> {
                     if (error instanceof NoSuchElementException) return Single.error(new IllegalArgumentException("User not found"));
                     return Single.error(error);
                 });
    } catch (Exception e) {
        return Single.error(e);
    }
});
```

**Message Structure**:
- Request
```json
{
    "id": 1,
    "timestamp": 1738415230282,
    "version": "r145.63cc2b3",
    "type": "req",
    "method": "auth",
    "params": {
        "email": "demo.student@uiu.ac.bd",
        "password": "WrongPass"
    },
    "sessionToken": null,
    "connection": null
}
```
- Success Response
```json
{
    "id": 1,
    "timestamp": 1738415232717,
    "version": "r145.63cc2b3",
    "type": "res",
    "method": "success",
    "params": {
        "sessionToken": "9361147112443298276352683881617408806308841024330508058726967887",
        "uuid": "af0f9edf-73c3-47cc-85c7-851e1ea99f36",
        "userType": "STUDENT"
    },
    "error": false,
    "errorMessage": null
}
```
- Error Response
```json
{
    "id": 1,
    "timestamp": 1738415230525,
    "version": "r145.63cc2b3",
    "type": "res",
    "method": "error",
    "params": {
        "message": "Invalid password"
    },
    "error": true,
    "errorMessage": "Invalid password"
}
```
- Heartbeat
```json
{
    "id": 8,
    "timestamp": 1738415442684,
    "version": "r145.63cc2b3",
    "type": "req",
    "method": "health",
    "params": null,
    "sessionToken": "9361147112443298276352683881617408806308841024330508058726967887",
    "connection": null
}
```
```json
{
    "id": 8,
    "timestamp": 1738415442686,
    "version": "r145.63cc2b3",
    "type": "res",
    "method": "success",
    "params": {
        "serverVersion": "r145.63cc2b3",
        "status": "ok",
        "serverTime": 1738415442686
    },
    "error": false,
    "errorMessage": null
}
```

**Connection Lifecycle**:
1. Client connects via `ClientRPC.start()`
2. Auth exchange using credentials
3. Server creates `SessionInfo` with 24h TTL
4. Heartbeats reset TTL on each message
5. Cleanup thread removes expired sessions

#### **Future Improvements**

- Add encryption in `RPCConnection`
- Session token rotation
- Protobuf for serialization
- Message compression for large payloads
- Idempotency keys for retries

### **Session Management**

**Implementation Status**: ✅ Complete

#### **What It Does**
- Maintains authenticated user sessions with time-to-live (TTL) tracking
- Binds network connections to user identities for stateful communication
- Automatically cleans up expired/inactive sessions to free resources

#### **How It Works**
1. **Session Creation**:
    - Generates unique 64-character token via `SecureRandom` on successful authentication
    - Stores session metadata (user ID, connection, timestamps) in `ConcurrentHashMap`
2. **Validation**:
    - Checks `isActive()` flag and `expirationTime` on every authenticated request
    - Updates `lastHeartbeat` timestamp on valid operations
3. **Cleanup**:
    - Scheduled thread scans sessions every 1 minute
    - Removes sessions exceeding 24-hour TTL or 30-second heartbeat timeout

#### **Session Storage**
```json
{
    "9361147112443298276352683881617408806308841024330508058726967887" : {
        "sessionToken" : "9361147112443298276352683881617408806308841024330508058726967887",
        "userId" : "af0f9edf-73c3-47cc-85c7-851e1ea99f36",
        "tags" : [ ],
        "createdAt" : 1738415232717,
        "lastHeartbeat" : 1738415262686,
        "expirationTime" : 1738501662686,
        "active" : true,
        "expired" : false,
        "ip" : "127.0.0.1"
    }
}
```

### **Rate Limiting**

Demo: ![rate-limited.png](readame-assets/rate-limited.png)

#### **What It Does**
- Enforces request quotas to prevent system overload
- Applies different limits for unauthenticated (IP-based) and authenticated (session-based) traffic
- Protects against DDoS and brute-force attacks

#### **How It Works**
1. **Counting Mechanism**:
    - Uses `ConcurrentHashMap` to track request counts per IP/session
    - Merges counts atomically using `Integer::sum`
2. **Limit Enforcement**:
    - Denies requests exceeding 32/minute for IPs (pre-auth)
    - Allows 512/minute for authenticated sessions
3. **Periodic Reset**:
    - Scheduled task clears all counters every 60 seconds

#### **Storage**:
```json
{
        "9361147112443298276352683881617408806308841024330508058726967887" : 1,
        "127.0.0.1" : 4
}
```

### **System Logging Infrastructure**

**Implementation Status**: ✅ Complete

#### **What It Does**
- Captures critical system metadata during startup
- Logs Java properties, environment variables, and hardware specs
- Provides runtime diagnostics for troubleshooting

#### **How It Works**

1. **Entry Point**:
   - `Issue.print(logger)` called during application bootstrap

2. **Data Collection**:
   - System Properties via `System.getProperties()`
   - Environment Variables via `System.getenv()`
   - Hardware Metrics using `OperatingSystemMXBean`

3. **Formatting**:
   - Section headers with indented key-value pairs
   - Memory values auto-converted to GB

**Example Output**:
```txt
JAVA SYSTEM PROPERTIES:
    file.encoding: UTF-8
    java.class.version: 67.0
    java.home: /usr/lib/jvm/java-23-openjdk
    ...

ENVIRONMENT VARIABLES:
    BROWSER: firefox
    EDITOR: micro
    LANG: en_US.UTF-8
    LANGUAGE: en_US:en:C
    LC_ADDRESS: C.utf8
    LC_COLLATE: C.utf8
    LC_CTYPE: en_US.UTF-8
    LC_IDENTIFICATION:
    LC_MEASUREMENT: C.utf8
    LC_MESSAGES: en_US.UTF-8
    LC_MONETARY: en_US.UTF-8
    LC_NAME: en_US.UTF-8
    LC_NUMERIC: en_US.UTF-8
    LC_PAPER: en_US.UTF-8
    LC_TELEPHONE: C.utf8
    LC_TIME: C.utf8
    ...

HARDWARE INFORMATION:
    OS Name: Linux
    OS Version: 6.12.10-zen1-1-zen
    OS Architecture: amd64
    Available Processors (Cores): 8
    Total Physical Memory: 23 GB
    Free Physical Memory: 8 GB
    Total Swap Space: 11 GB
    Free Swap Space: 6 GB
```

#### **Why It Exists**
- Essential for debugging environment-specific issues
- Provides visibility into resource constraints

#### **Future Improvements**
- Add disk space monitoring
- Periodic runtime snapshots
- Integration with monitoring systems like Prometheus

### **Semantic Version Generation**

**Implementation Status**: ✅ Complete

#### **What It Does**
- Generates version strings from Git history
- Formats as `r<commit_count>.<short_hash>` (e.g., `r145.63cc2b3`)
- Automatically generates new one at build

#### **Implementation**

```java
String v = Version.getVersion();
```

#### **Workflow**
1. Build process executes `generateVersion()` Gradle task
2. Writes version to `src/main/resources/version.properties`
3. Application loads via static initializer

#### **Why It Exists**
- Eliminates manual version tracking
- Links deployments to exact code states

#### **Future Improvements**
- Nightly build automation
- Version metadata endpoint in RPC system

### **Internationalization**

**Implementation Status**: 🟡 Partial (90%)

**Demo**: ![i18n.mp4](readame-assets/i18n.mp4)

#### **What It Does**
- Can support more languages by editing an enum
- Centralized translation management
- Hot-reload language without restart

#### **Architecture**
1. **Resource Bundles**:
   - `messages_en.properties`: English translations
   - `messages_bn.properties`: Bengali translations

2. **Validation**:
   - Startup check for missing keys in all bundles

3. **Reactive Updates**:
   - `SettingsManager` notifies `I18nManager` of language changes

**Usage**:
```java
String greeting = I18nManager.instance.get(TranslationKey.HELLO);
```

#### **Future Improvements**
- Add right-to-left (RTL) layout support
- Dynamic bundle reloading from filesystem
- Pluralization rules implementation

### **Global State Management**

**Implementation Status**: ✅ Complete

#### **What It Does**
- Thread-safe shared memory for cross-component data
- Stores session tokens, UI states, and temporary workflows

#### **Implementation**
```java
// Store current user
Volatile.getInstance().put("currentUser", user);
```
```java
// Retrieve chat history
List<Message> chat = (List<Message>) Volatile.getInstance().get("activeChat");
```

**Key Features**:
- ConcurrentHashMap backend for scalability
- Audit logging on write/delete operations
- Singleton access via double-checked locking

#### **Future Improvements**
- TTL-based auto-expiry for entries
- Size monitoring and eviction policies
- Cluster-aware replication for distributed mode

### **Persistent User Settings**

**Implementation Status**: ✅ Complete

**Demo**: ![settings.json.png](readame-assets/settings.json.png)

#### **What It Does**
- Maintains 9+ user preferences across sessions
- OS-appropriate config file storage
- Type-safe validation with reactive updates

#### **Data Flow**

![settings.png](readame-assets/settings.png)

**Key Features**:
- Cross-platform config paths:
    - Windows: `%APPDATA%\enrollium\settings.json`
    - Linux: `~/.config/enrollium/settings.json`
    - macOS: `~/Library/Application Support/enrollium/settings.json`
- Versioned schema migrations
- Debounced autosave (1-second delay)

#### **Why It Exists**
- Essential for personalized user experience
- Reduces setup friction across devices

#### **Future Improvements**
- Cloud sync via RPC system
- Settings import/export UI
- Historical version rollback

### **Chat**

**Key Features**
- Real-time messaging for smooth conversations.
- Saves chat history so you never lose track.
- Customize notifications, themes, and more.


**What’s Next**
- Sync chats across devices with cloud support.
- Add more fun options like emojis and custom fonts.
- Let users search through old messages easily.

### **Offered Courses**

**Key Features**
- A searchable list of all available courses.
- Details like schedules, instructors, and prerequisites.
- Filter and sort courses to find what you need.


**What’s Next**
- Add more filters like course format or language.
- Help users track their progress in enrolled courses.

### **Section Selection**

**Key Features**
- Pick course sections based on schedule.
- See registration status in real-time.
- Check how many seats are left in a section.
- Get added to a waitlist if a section is full.

**What’s Next**
- Suggest sections that fit  preferences.
- Warn you if a section clashes with your schedule.
- Send reminders for deadlines or waitlist updates.
- Sync course schedule with your calendar.

### **Trade**

**Key Features**
- List items or services you want to trade.
- Search and filter to find what you need.
- Negotiate deals through chat or offers.
- Rate and review trade partners for trust.

**What’s Next**
- Add extra security for high-value trades.
- Show users their past trade history.

### **Server Status**

**Key Features**
- Live updates on server health and uptime.
- Alerts for maintenance or outages.
- Track past server performance.
- Get notifications via email or SMS.

**What’s Next**
- Predict server issues before they happen.
- Let users customize how they get alerts.
- Track server status across different regions.
- Show how server issues might affect specific features.

### **Send Notification**

**Key Features**
- Send alerts via email, SMS, or push notifications.
- Customize messages based on user actions.
- Notifications go out instantly when needed.


**What’s Next**
- Add images or buttons to make alerts more engaging.
- Support notifications in multiple languages.
- Provide detailed reports on notification performance.

### **Withdraw**

**Key Features**

- Supports multiple withdrawal methods.
- Shows fees and limits upfront.
- Keeps a record of all past withdrawals.

**What’s Next**
- Speed up withdrawal processing times.
- Support more currencies for global users.
- Let users set up recurring withdrawals.

### **Comprehensive Database Management**

**Key Features**
- Tracks course, faculty, and student info.
- Manages schedules, rooms, and prerequisites.
- Links with notifications for updates.
- Keeps everything organized and easy to access.

**What’s Next**
- Add more filters for better search options.
- Sync changes across the platform instantly.
- Provide insights into enrollment trends.

### **Theme Change**

**Key Features**
- Switch between light and dark mode.
- Choose from pre-designed themes or create your own.
- Auto-switch themes based on time of day.


**What’s Next**
- Adjust themes based on lighting or user preferences.
- Let users customize fonts, spacing, and colors.
- Add fun themes for holidays or special events.
- Save theme preferences across all devices.

### **Login, User Info, and Forgot Password**

**Key Features**
- Secure login with passwords, MFA, and CAPTCHA.
- Update your profile info easily.
- Reset forgotten passwords via email or SMS.
- Log out of other devices remotely.

**What’s Next**
- Use magic links or one-time codes instead of passwords.
- Notify users of suspicious activity on their account.
- Let users log in once to access multiple platforms.

### **History**

**Key Features**
- Tracks all your actions with timestamps.
- Search and filter to find specific entries.
- Revert to a previous state with one click.
- Export your history for offline use.

**What’s Next**
- Show history as a visual timeline or graph.
- Let teams view shared activity logs.
- Add encryption to protect sensitive history data.
