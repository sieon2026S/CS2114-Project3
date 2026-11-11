# CS2114-Project3: Table Time (TT)

Table Time is a digital waitlist for a restaurant host stand. The host adds walk-in parties (name and size), and they wait in line in arrival order. When the host clicks an open table, the app seats the party that has waited longest and actually fits there. A party too big for that table keeps its place for the next big table.

The waitlist is backed by **three singly linked `LinkedQueue<Party>` queues**, one per size group: small (1–2), medium (3–4) and large (5–6). Each queue is strictly first-in, first-out. Seating only ever looks at queue fronts, so it never has to remove from the middle of a queue.

## How to run

**Setup:** in Eclipse, import the repo (File → Import → Git → Projects from Git). The project uses Java 8 and the course's `CS2-Support` project for `student.TestCase`, the same as our labs. `CS2-Support` must be in your workspace.

| What | How |
|---|---|
| **Model tests (no view needed)** | Right-click `src/model` → Run As → JUnit Test. Runs `LinkedQueueTest`, `PartyTest`, `WaitlistTest` and `RestaurantTest` (34 tests). None of them create a window or controller. |
| **Thin view (the app)** | Open `src/controller/TableTimeApp.java` → Run As → Java Application. |

**Trying the app:** type a name and size, then click **Add**. Click a **green** (open) table to seat the next party that fits, and a **red** (taken) table to free it. **Remove** takes a walk-away off the list by name. **Undo** removes the most recent add. Errors appear on the bottom line, for example name `@d@m`, size `0`, `four`, `1000000000`, or a duplicate name.

## Package layout (MVC)

| Package | Role | Classes |
|---|---|---|
| `model` | State and logic. Never imports a GUI class. | `LinkedQueue<T>`, `Party`, `SizeGroup`, `Table`, `Waitlist`, `Restaurant`, `RestaurantListener` |
| `view` | Presentation only | `TableTimeWindow` |
| `controller` | Turns clicks into model calls | `HostController`, `TableTimeApp` |

The view reads the model through `getWaitingParties()` and `getTables()`, which return copies. The view sends every button press to `HostController`, and only the controller calls methods that change the model. The model tells the view to redraw through `RestaurantListener.modelChanged()` (the observer pattern).

---

# Reflection

### What changed from our specification?

| Area | What the spec said | What we built | Why / what prompted it |
|---|---|---|---|
| **Method signatures** | `addParty`, `seatAt`, `removeParty`, `undoLastAdd` return `void` and throw on bad input | `addParty` and `seatAt` return the `Party`. `seatAt` returns `null` if nobody fits. `removeParty` returns `boolean`. `undoLastAdd` returns the `Party`, or `null` if there's nothing to undo. `freeTable` returns `boolean`. | **Prompted by writing the controller.** `HostController` needs to tell the host *who* was seated or added. Showing "nobody fits table 1" is a normal host situation, not an error, so returning `null`/`false` fit better than an exception. |
| **`getWaitingParties` / `getTables`** | Return `List<Party>` / `List<Table>` copies | Same. Both return new lists, so editing them can't change the model. | No change. Confirmed by `RestaurantTest.testReturnedListsAreCopies`. |
| **`LinkedQueue`** | `enqueue`, `dequeue`, `peek`, `isEmpty`, plus an O(n) `remove` | Same, plus `size()` and `toList()`. `peek()` on an empty queue returns `null` instead of throwing. | **Prompted by `Waitlist`.** It needs to list the waiting parties in arrival order for the screen, so we added `toList()`. Returning `null` from `peek()` let `Waitlist.nextFor` check queue fronts without try/catch. |
| **`enqueue(null)`** | Throws (Test Plan) | The first version accepted `null`. It now throws `IllegalArgumentException`. | **Prompted by a failing test:** `LinkedQueueTest.testEnqueueNullThrows`. |
| **Where validation lives** | `HostController` and the `Party` constructor | Same split. The controller only turns text into an `int` (`"four"` and `"2.5"` are caught there). `Party` rejects sizes outside 1–6 and names that aren't letters, spaces, hyphens or apostrophes (30 max). `Restaurant.addParty` rejects duplicate names that are waiting or seated, ignoring case. | **Prompted by failing tests.** The first `Party` only checked `size <= 0` and blank names, so `PartyTest.testBadSizesThrow` (size 7, 1000000000) and `testBadNamesThrow` (`@d@m`, `Z010`) failed. We added the upper size limit and the name rule. |
| **Packages** | One model package | `model`, `view` and `controller` packages | **Prompted by integration.** The controller originally had no `package` line and its files were missing `.java`. Java can't import classes from the default package, so each MVC layer got its own package. |
| **Classes** | 11 types | Same 11 types | No change. |
| **Test cases** | One normal and one bad case per method in the Test Plan | All Test Plan rows, plus every bad-input case from our scope (34 tests) | **Prompted by the Exceeds rubric** ("survives the bad-input cases named in the scope"). The tests for removing "Nobody" and undo on an empty list accept either throwing or a `false`/`null` return, because the spec and the controller disagreed until we chose return values. |

### Did the model really stand alone?

**Yes.** Nothing in `model` imports `java.awt`, `javax.swing`, `view` or `controller`. All 34 tests build a `Restaurant` with no window, using a small `CountingListener` in place of the GUI. That listener checks that each real change sends exactly one `modelChanged()` and that rejected actions send none. Nothing moved across the model/view line. The only cross-layer link is the `RestaurantListener` interface, which the model owns and the view implements.

### What happened to our unknowns?

| Unknown (from our scope) | Status |
|---|---|
| **Sathvik:** how to seat "the first party that fits" when the queue only removes from the front | **Solved.** We use three size-group queues. `Waitlist.nextFor(capacity)` takes the front of the matching group first, otherwise the earliest-arriving front among smaller groups. It only peeks at and dequeues queue fronts, so seating is O(1). |
| **Sieon:** what happens when a party of 2 could take a six-top | **Solved.** A table serves its own size group first and only falls back to smaller parties when that group is empty. The six-top goes to a waiting party of 5–6 if there is one. |
| **Charlie:** combining two tables for large parties | **Still open.** Party size is capped at 6 for the MVP. This is our change feature (see Next Steps). |
| **Zolo:** realistic wait-time estimates | **Still open.** It stays a stretch goal. |

### What doesn't work yet?

- Parties larger than 6 are rejected. There's no table combining yet.
- No wait-time estimates, and no timer that moves no-shows to the back.
- Nothing is saved between runs.
- Tests cover only the model. The controller and view were tested by hand: every scope bad-input case was clicked through the running app with no crash.
- All 34 model tests pass, and no tests are failing.

---

# Next Steps

### What's left to finish the MVP (in order)

1. Merge the `model`, `view`, `sieon` and `tests` branches into `main`, and delete the old `src/tabletime` and `src/Controller` folders.
2. Have each member run the full app and test suite from `main`.
3. Polish the error messages and add a short "how to use" label to the window.

### Our change feature: **Combine Tables**

Let the host join two open 4-tops so a party of 7–8 can be seated, instead of rejecting any party over 6.

**Predicted to change:**

| Class | Layer | Methods we expect to change or add |
|---|---|---|
| `Party` | model | Constructor: raise `MAX_SIZE` from 6 to 8 |
| `SizeGroup` | model | Add `XLARGE(7, 8)`, and update `forSize(int)` and `fitsAt(int)` |
| `Waitlist` | model | New queue for `XLARGE`; `queueFor(SizeGroup)`; `nextFor(int capacity)` handles capacity 8 |
| `Restaurant` | model | New `seatCombined(int tableA, int tableB)`. `freeTable(int)` must free both joined tables. |
| `Table` | model | New field linking to the joined table, plus `isJoined()` and `getJoinedWith()` |
| `HostController` | controller | New `handleCombine(int a, int b)`, and `handleTableClick` frees both halves |
| `TableTimeWindow` | view | New **Combine** button, and `modelChanged()` labels joined tables |

**Predicted *not* to change:** `LinkedQueue<T>` (the queue doesn't know about tables), `RestaurantListener`, `TableTimeApp`, and the existing tests for the queue and seating.

### Stretch goals

| Goal | Decision |
|---|---|
| Combine tables | **Committing.** This is our change feature. |
| Wait-time estimates | **Committing**, if time allows after combining |
| No-show timer (move to the back after 5 minutes) | **Dropping.** It needs timers on top of the GUI, which is too much risk for the time left. |
| End-of-night summary | **Dropping** |

### New tests needed

- `SizeGroup.forSize(7)` and `forSize(8)` return `XLARGE`; `forSize(9)` throws
- `Party` sizes 7 and 8 are accepted, and 9 is rejected
- `Restaurant.seatCombined(5, 6)`:
  - normal case: a party of 8 is seated and both tables are taken
  - bad input: a table is already taken, the same table is given twice, or the tables aren't 4-tops, which should throw and change nothing
- `freeTable` on one half of a combined table frees both halves
- `Waitlist.nextFor(8)` returns the earliest `XLARGE` party, and falls back to smaller groups when the `XLARGE` queue is empty

### Who does what

| Item | Owner |
|---|---|
| Merge branches into `main` and clean up old folders | Sieon |
| `Party`, `SizeGroup` and `Waitlist` changes for sizes 7–8 | Zolo |
| `Table` and `Restaurant.seatCombined` / `freeTable` | Sathvik |
| `HostController.handleCombine` | Sieon |
| **Combine** button and joined-table labels in `TableTimeWindow` | Charlie |
| New tests listed above | Sathvik and Charlie |
| Wait-time estimates (if time allows) | Zolo |

---

# Attribution

| Class / test file | Who worked on it | How |
|---|---|---|
| `LinkedQueue<T>` (+ `Node<T>`) | Zolo; Sathvik | Zolo wrote it. Sathvik added the `null` check and `toList()` (GenAI-assisted). |
| `Party` | Zolo; Sathvik | Zolo wrote it. Sathvik fixed a compile error in `equals` and added size 1–6 and name validation (GenAI-assisted). |
| `RestaurantListener` | Zolo | Wrote |
| `SizeGroup` | Sathvik | Wrote (GenAI-assisted) |
| `Table` | Sathvik | Wrote (GenAI-assisted) |
| `Waitlist` | Sathvik | Wrote (GenAI-assisted) |
| `Restaurant` | Sathvik | Wrote (GenAI-assisted) |
| `TableTimeWindow` | Sathvik | Wrote (GenAI-assisted), covering the view |
| `HostController` | Sieon; Sathvik | Sieon wrote it (GenAI-assisted). Sathvik fixed the package line and imports. |
| `TableTimeApp` | Sieon; Sathvik | Sieon wrote it. Sathvik moved the code into `main(...)` and fixed the imports. |
| `LinkedQueueTest` | Sathvik | Wrote from the Test Plan (GenAI-assisted) |
| `PartyTest` | Sathvik | Wrote from the Test Plan (GenAI-assisted) |
| `WaitlistTest` | Sathvik | Wrote from the Test Plan (GenAI-assisted) |
| `RestaurantTest` | Sathvik | Wrote from the Test Plan (GenAI-assisted) |
| `README.md` | Sieon; Sathvik | Sieon set up the template. Sathvik wrote it (GenAI-assisted). |

## GenAI note

We used **Claude** for the following:
- **Tests:** we asked it to turn our spec's Test Plan into JUnit tests in the `student.TestCase` style from our Project 2 repo. We kept the test cases. We changed them to use Zolo's method names (`size()`) and the `model` package, and loosened two tests so they accept either a thrown exception or a `false`/`null` return.
- **Remaining model classes:** we asked it to write `SizeGroup`, `Table`, `Waitlist` and `Restaurant` to match our spec and the return values `HostController` expects. We kept the three-queue seating rule from our spec.
- **Integration problems:** it found the compile errors (a broken line in `Party.equals`, a duplicate `package` line, files missing `.java`). It also found that `Party` accepted sizes over 6 and invalid names. We fixed all of these.
- **The thin view:** we asked it to draft `TableTimeWindow` from the fields in our spec.
- **README:** we asked it to draft this file. We checked every claim against the code, and the test count by running the tests.

`HostController` was also written with GenAI help, by Sieon.
