# **ENROLIUM**

## **1. Summary**

ENROLIUM was born out of the frustration students faced during chaotic course enrollment periods, where outdated systems often left learners scrambling for sections or stuck with incompatible schedules.
This project reimagines enrollment as a collaborative, transparent process rather than a zero-sum race. At its core, ENROLIUM ensures fairness through randomized lotteries during high-demand registration windows while empowering students with tools like *real-time trade matching*—a graph-based system that lets users swap sections like puzzle pieces until everyone’s schedule clicks into place.
Beyond individual needs, it acknowledges the reality of group learning: students can team up to coordinate schedules, though the system deliberately limits group advantages to prevent monopolization.
The clean JavaFX interface hides sophisticated backend mechanics—atomic seat reservations, session-aware RPC calls, and ACID-compliant transactions—behind a deliberately minimalist design, prioritizing reliability over flashy visuals.

---

## **2. System Overview**

### **2.1 Architecture Diagram**

![High-Level Architecture](path/to/architecture.png)
*(Annotate components: UI, RPC, DB, etc.)*

### **2.2 Technology Stack**

| Layer         | Technology    | Purpose                          |
|---------------|---------------|----------------------------------|
| Frontend      | JavaFX 17     | User interface rendering         |
| Communication | gRPC/gSocket  | Bidirectional RPC implementation |
| Database      | PostgreSQL 15 | Data persistence                 |

---

## **3. Feature Documentation**

*Repeat for each feature including the database and RPC system*

---

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

```markdown
1. **Workflow**:
    - Step 1: User triggers [Action] in `MainUI.java`
    - Step 2: RPC call via `RpcService.sendRequest()`
    - Step 3: Database transaction in `InventoryDAO.updateStock()`

2. **Key Classes**:
    - `FeatureController.java`: Handles UI logic
    - `DataProcessor.java`: Business logic

3. **Patterns/Concepts**:
    - Observer pattern for UI updates
    - Connection pooling for DB access
```

#### **Why It Exists**

- Business need: "Required for compliance with [Regulation X]"
- Technical rationale: "Replaces legacy system bottleneck"

#### **Future Improvements**

```markdown
- **Performance**: "Replace ArrayList with HashMap for O(1) lookups"
- **Functionality**: "Add bulk edit support (partially implemented in `BulkEditBranch`)"
- **UX**: "Tooltips for form fields shown in [Fig 3.X]"
```

---

### **3.1 Database System**

**Implementation Status**: ✅ Complete

**Demo**:
![DB Schema Overview](path/to/er_diagram.png)
*(Key tables: `users`, `courses`, `subjects`, `sections`, `trimesters`, `notifications`)*

---

#### **What It Does**

- Single source of truth.
- Enforces ACID-compliant transactions for all critical operations.
- Supports complex relationships (e.g., prerequisites, trimester phases, section-teacher assignments).
- Generates demo data.

---

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

---

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
DB.read(Course.class, "createdAt", true, 10, 0)
  .filter(course -> course.getStatus() == CourseStatus.REGISTERED)
  .subscribe(
      course -> System.out.println("Course: " + course.getSubject().getName()),
      error -> System.err.println("Error: " + error.getMessage())
  );
```

#### **3. Updating an Entity**

```java
// Update a student's email
DB.findById(Student.class, studentId)
  .subscribe(student -> {
      student.setEmail("new.email@uiu.ac.bd");
      DB.update(student).subscribe(
          updated -> System.out.println("Updated!"),
          error -> System.err.println("Update failed: " + error.getMessage())
      );
  });
```

#### **4. Deleting an Entity**

```java
// Delete a notification by ID
DB.delete(Notification.class, notificationId)
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

---

#### **Future Improvements**

- Cache frequently accessed entities like `subjects` and `trimesters`.
- Implement database-level triggers for cross-table validations (e.g., section capacity checks).


### **3.Y Bidirectional RPC**

**Implementation Status**: 🟡 Partial (Auth missing)

**Demo**:
![RPC Sequence Diagram](path/to/rpc_flow.png)

#### **What It Does**

- Enables real-time server→client notifications
- Handles 150+ concurrent requests

#### **How It Works**

```markdown
1. **Protocol**: Protobuf schema in `message.proto`
2. **Flow**:
    - Client → Server: `Request.newBuilder().setUserId(...)`
    - Server → Client: Streaming `Response` via `Observer.onNext()`
3. **Key Classes**: `RpcClientManager.java`, `MessageDispatcher.java`
```

#### **Why It Exists**

- "Eliminate polling for inventory updates"
- "Low-latency requirement (<200ms)"

#### **Future Improvements**

```markdown
- **Security**: Add OAuth2 handshake
- **Recovery**: Implement retry-with-backoff in `RpcRetryHandler`
```

---

## **4. Appendices**

- **A1**: Full DB schema PDF
- **A2**: Annotated code snippets for complex features
- **A3**: RPC message samples (JSON/Protobuf)
- **A4**: Setup/usage video (15 mins)

