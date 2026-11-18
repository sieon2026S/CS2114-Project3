# CS2114-Project3: Table Time (TT)

Table Time is a digital waitlist for a restaurant host stand. The host adds walk-in parties (name and size), and they wait in line in arrival order. When the host clicks an open table, the app seats the party that has waited longest and actually fits there. A party too big for that table keeps its place for the next big table.

**New in Deliverable 4: Combine Tables.** Parties of 7–8 can now join the waitlist. The host seats them by pushing two open 4-tops together. Freeing either half frees both.

The waitlist is backed by **four singly linked `LinkedQueue<Party>` queues**, one per size group: small (1–2), medium (3–4), large (5–6) and extra-large (7–8). Each queue is strictly first-in, first-out. Seating only ever looks at queue fronts, so it never removes from the middle of a queue.

## How to run

**Setup:** in Eclipse, import the repo (File → Import → Git → Projects from Git). The project uses Java 8 and the course's `CS2-Support` project for `student.TestCase`, the same as our labs. `CS2-Support` must be in your workspace.

| What | How |
|---|---|
| **All tests (no view needed)** | Right-click `src` → Run As → JUnit Test. Runs 7 test classes and **64 tests**: `LinkedQueueTest`, `PartyTest`, `SizeGroupTest`, `TableTest`, `WaitlistTest` and `RestaurantTest` in `model`, plus `HostControllerInputTest` in `controller`. None of them create a window. |
| **The app** | Open `src/controller/TableTimeApp.java` → Run As → Java Application. |

**Using the app**
- Type a name and size (1–8) and click **Add**.
- Click a **green** (open) table to seat the next party that fits. Click a **red** (taken) table to free it.
- **Remove** takes a walk-away off the list by name. **Undo** removes the most recent add.
- **Combine:** for a party of 7–8, type two open 4-top numbers (5, 6 or 7) in the Combine boxes and click **Combine**. Both tables turn red and show "joined with …". Clicking either one frees both.
- Errors appear on the bottom line. The app never crashes on bad input.

## Package layout (MVC)

| Package | Role | Classes |
|---|---|---|
| `model` | State and logic. Never imports a GUI class. | `LinkedQueue<T>`, `Party`, `SizeGroup`, `Table`, `Waitlist`, `Restaurant`, `RestaurantListener` |
| `view` | Presentation only | `TableTimeWindow` |
| `controller` | Turns clicks into model calls | `HostController`, `TableTimeApp` |

User actions flow **view → controller → model**. Every button calls a `HostController.handle…` method, and only the controller calls model methods that change state. The model tells the view to redraw through `RestaurantListener.modelChanged()` (the observer pattern). The view then re-reads `getWaitingParties()` and `getTables()`, which return copies.

**The view keeps no copy of model state.** `TableTimeWindow`'s only fields are the `Restaurant` reference, the controller, and Swing widgets: text boxes, buttons, the waitlist text area and the message label. It never stores a list of parties or tables, or a count, or a "currently seated" value. On every `modelChanged()` it reads `getWaitingParties()` and `getTables()` again and redraws from them, then lets them go. So the screen can't drift out of sync with the model, and a new model feature like joined tables only needed new *reads* in the view.

---

# Reflection

### How did our design absorb the change?

Our Deliverable 3 prediction, compared with what we actually touched:

| Class | Predicted (D3) | What actually changed (D4) | Match? |
|---|---|---|---|
| `Party` | Raise `MAX_SIZE` from 6 to 8 | `MAX_SIZE = 8` and an updated error message. No other change. | ✅ Exactly as predicted |
| `SizeGroup` | Add `XLARGE(7, 8)`; update `forSize` and `fitsAt` | Added `XLARGE(7, 8)`. `forSize` and `fitsAt` **didn't need any code change**, because they already loop over `values()`. | ✅ Smaller than predicted |
| `Waitlist` | New `XLARGE` queue; `queueFor`; `nextFor(int)` handles capacity 8 | New `xlarge` queue; `queueFor`, `getWaitingInOrder` and `size` include it. `nextFor(8)` returns **only** a 7–8 party. | ✅ Matched, with a rule change (see below) |
| `Restaurant` | New `seatCombined(int, int)`; `freeTable(int)` frees both | Exactly that | ✅ Matched |
| `Table` | Joined-table field, `isJoined()`, `getJoinedWith()` | Those, plus package-private `joinWith(Table)` and `unjoin()` so only the model can link tables | ✅ Matched, plus 2 helper methods |
| `HostController` | New `handleCombine(int a, int b)`; `handleTableClick` frees both | New `handleCombine(String, String)`, which takes **raw text** like `handleAdd` does. `handleTableClick`'s message now names both freed tables. | ⚠️ Signature differs (see below) |
| `TableTimeWindow` | Combine button; `modelChanged()` labels joined tables | A second row with two table-number boxes and a **Combine** button. `modelChanged()` adds "(joined with N)". New `clearCombineInputs()`. | ✅ Matched |
| `LinkedQueue<T>` | **Should not change** | **Didn't change** | ✅ |
| `RestaurantListener` | **Should not change** | **Didn't change** | ✅ |
| `TableTimeApp` | **Should not change** | **Didn't change** | ✅ |

**What surprised us**
1. **Three size-limit tests went red.** As predicted, no queue or seating test changed. We'd also planned new tests for "sizes 7 and 8 accepted, 9 rejected". What we didn't expect was that **three existing** bad-input tests used **7** as their example of a too-big party: `PartyTest.testBadSizesThrow`, `RestaurantTest.testAddPartyBadSizes`, and Charlie's `SizeGroupTest.testForSizeBadSizesThrow`. They failed the moment we raised the limit, which is exactly what tests are for. We changed the example to 9. They're size-validation tests, not queue or seating tests, so our "should not change" list still held.
2. **`nextFor(8)` needed a different rule.** Our usual rule is "matching group first, otherwise the oldest smaller party." For two combined tables, that rule would push two 4-tops together for a couple, wasting both. So capacity 8 only ever seats a 7–8 party. That one rule decision is covered by its own test (`testNextForEightNoExtraLargeReturnsNull`).
3. **`SizeGroup` was easier than we thought.** Because `forSize` and `fitsAt` already loop over `values()`, adding one enum constant was the whole change.
4. **A stale message in the controller.** `handleAdd` still said "1 to 6" when the size wasn't a number. We found it during our bad-input run-through, not from a test.

### Did the change cross the model/view line?

**Only in the expected, read-only way.** The view had to learn two new read-only facts about a `Table`, `isJoined()` and `getJoinedWith()`, so it can label joined tables. It still can't change the model: `joinWith`, `unjoin`, `seat` and `clear` are package-private, so only `model` classes can call them.

Nothing rippled where it shouldn't have:
- `LinkedQueue`, `RestaurantListener` and `TableTimeApp` are untouched. **No class we predicted would stay unchanged had to change.** The only edit near the queue is two *new* interleaving tests added to `LinkedQueueTest` for coverage (see below). The feature didn't require them, and no existing queue test was edited.
- The model still imports nothing from `view` or `controller`.
- All 64 tests still run with no window.
- The observer mechanism needed no changes. Combining or freeing tables sends **one** `modelChanged()`, just like seating does, and that's tested.

### What changed from our D3 plan?

| Planned | What we did | Why |
|---|---|---|
| `HostController.handleCombine(int a, int b)` | `handleCombine(String, String)` | To match `handleAdd`: the view passes raw text, and only the controller turns it into numbers. Text like `"five"` or blank boxes then gets a friendly message instead of being an error the view has to handle. |
| `Waitlist.nextFor(8)` uses the normal fallback | Capacity 8 seats **only** a 7–8 party | So two tables aren't joined for a party that would fit at one (see Surprise 2) |
| Bad input for combine: "a table taken, the same table twice, not 4-tops" | All of those, plus a table that doesn't exist (`99`, `0`), non-numbers, and "nobody of 7–8 is waiting" | We found these while testing the running app. Each one now shows a message. |
| New tests listed in D3 | All written. Joined-table and `XLARGE` cases were added to Charlie's `TableTest` and `SizeGroupTest`. | D4 requires a normal and a bad-input test for **every** core model method, including new ones like `Table.joinWith` and `unjoin`. Charlie had already written direct tests for `Table` and `SizeGroup` after D3, so we extended his files instead of starting over. We also moved them from `src/` into `src/model/` to match their `package model;` line. |
| Wait-time estimates "if time allows" | Not started | Combine Tables took the time. Moved to the final build (see Next Steps). |
| Controller tested only by hand | Added `HostController.parseWholeNumber(String)` and `HostControllerInputTest` | `"2.5"`, `"four"`, `"99999999999"` and blank Combine boxes never reach the model, so model tests can't cover them. Pulling the parsing into one static method made it testable without creating a window, and both `handleAdd` and `handleCombine` now use it. |
| Queue tests unchanged | Same, plus two **interleaved** enqueue/dequeue tests | To lock in FIFO order when adds and removes are mixed, which is how a real host uses the line. `WaitlistTest` got a matching interleaved add/seat test. |

### What doesn't work yet?

- **Only two tables can be combined**, and only 4-tops (5, 6, 7), so the largest party is 8. Parties of 9+ are rejected with a message.
- Table 8 (the six-top) can't join anything. We decided that on purpose to keep the rule simple.
- **No wait-time estimates** yet.
- **No no-show timer** (dropped; see below).
- Nothing is saved between runs.
- **No automated tests for the view.** The window was checked by hand. The controller's only input rule, parsing text into numbers, has its own test.
- **Tests:** all 64 pass. None are failing or skipped.

### Every bad-input case has a test

Each case below was also run through the live app with no crash.

| Bad input | Where it's caught | Test |
|---|---|---|
| Name `@d@m`, `Z010` (invalid characters) | `Party` constructor | `PartyTest.testBadNamesThrow`, `RestaurantTest.testAddPartyBadNames` |
| Empty or blank name (`""`, `"   "`, `null`) | `Party` constructor | `PartyTest.testBadNamesThrow`, `RestaurantTest.testAddPartyBadNames` |
| Size `0`, `-3` | `Party` constructor | `PartyTest.testBadSizesThrow`, `RestaurantTest.testAddPartyBadSizes` |
| Extreme size `9`, `20`, `1000000000` | `Party` constructor | `PartyTest.testBadSizesThrow`, `RestaurantTest.testAddPartyBadSizes`, `SizeGroupTest.testForSizeBadSizesThrow` |
| Size `2.5`, `four`, `99999999999` (not a whole number) | `HostController.parseWholeNumber` | `HostControllerInputTest.testParseWholeNumberBadText` |
| Duplicate name (`Smith`, `" smith "`, already seated) | `Restaurant.addParty` | `RestaurantTest.testAddPartyDuplicateName`, `testAddPartyDuplicateIgnoresCaseAndSeated` |
| Removing a party that isn't there (`Nobody`) | `Restaurant.removeParty` | `RestaurantTest.testRemovePartyNotWaiting` |
| Clearing an already-empty table | `Restaurant.freeTable` | `RestaurantTest.testFreeAlreadyFreeTable`, `TableTest.testClearFreeTable` |
| Undo with nothing to undo | `Restaurant.undoLastAdd` | `RestaurantTest.testUndoOnEmptyList` |
| Seating at a taken table or table `99` | `Restaurant.seatAt` | `RestaurantTest.testSeatAtOccupiedTable`, `testSeatAtMissingTable` |
| **New:** Combine with blank boxes or `five` | `HostController.parseWholeNumber` | `HostControllerInputTest.testParseWholeNumberBadText` |
| **New:** Combine `5 + 5`, `5 + 99`, `0 + 6`, `1 + 5` (a 2-top), `7 + 8` (the six-top) | `Restaurant.seatCombined` | `RestaurantTest.testSeatCombinedBadTables` |
| **New:** Combine with a taken table | `Restaurant.seatCombined` | `RestaurantTest.testSeatCombinedTakenTable` |
| **New:** Combine when nobody of 7–8 is waiting | `Restaurant.seatCombined` / `Waitlist.nextFor(8)` | `RestaurantTest.testSeatCombinedNobodyLargeEnough`, `WaitlistTest.testNextForEightNoExtraLargeReturnsNull` |
| **New:** Seating at one half of a combined table | `Restaurant.seatAt` | `RestaurantTest.testSeatAtCombinedTableThrows` |
| **New:** Joining a table with itself or `null` | `Table.joinWith` | `TableTest.testJoinBadInputThrows` |

---

# Next Steps

### What's left for the final build (in order)

1. **Wait-time estimates.** `Restaurant` records when each table is freed, so the window can show "about N minutes" next to each waiting party.
2. **Controller tests.** We already have `HostControllerInputTest`. Next, add a `HostControllerTest` with a fake window so the exact on-screen messages are tested too.
3. **UI polish.** Disable **Combine** when nobody of 7–8 is waiting, and show each party's position and wait time in the list.
4. **Final README and demo rehearsal** with the whole team.

### A design change that would make the next feature cheaper

Our next feature is wait-time estimates. Two small changes would make it, and any later feature, much cheaper:

1. **Give `Restaurant` a clock instead of having it call the system time.** Add a one-method `TimeSource` interface with `long now()`. `Restaurant` takes one in its constructor, `TableTimeApp` passes the real clock, and tests pass a fake clock they can move forward ("10 minutes later"). Without it, every wait-time test would have to actually wait or would be flaky. With it, `Party` records its join time and `Restaurant` records when each table is freed, and all of that is testable in milliseconds.
2. **Have `Waitlist` keep its queues in an `EnumMap<SizeGroup, LinkedQueue<Party>>`** instead of four named fields and a `switch`. This feature showed the cost: adding `XLARGE` meant editing a field, the `switch` in `queueFor`, `getWaitingInOrder` and `size`. With a map that loops over `SizeGroup.values()`, a new size group would be a one-line change to `SizeGroup` and nothing in `Waitlist`.

### Stretch goals

| Goal | Decision |
|---|---|
| Combine tables | ✅ **Done** in Deliverable 4 |
| Wait-time estimates | ✅ **Committing** for the final build |
| No-show timer (move to the back after 5 minutes) | ❌ **Dropping.** It needs background timers in the GUI, which is too risky to test properly in the time left. |
| End-of-night summary | ❌ **Dropping** |

### Our 3-minute demo (timed: ends at 3:00)

Have `TableTimeApp` and the JUnit view already open so no time is spent launching.

| Time | Step (length) | What we show and say |
|---|---|---|
| 0:00–0:20 | **Tests** (20 s) | JUnit tab: **64/64 green**. "The model runs with no view, and every bad input has a test." |
| 0:20–1:05 | **Normal path** (45 s) | Add `Smith` 2, `Kim` 4, `Garcia` 6. Click table 1 → Smith, table 5 → Kim, table 8 → Garcia. "Four FIFO queues by size; seating only looks at queue fronts." |
| 1:05–1:55 | **New feature: Combine** (50 s) | Add `Patel` 8. Click a free 2-top: Patel isn't seated. Type `6` + `7` → **Combine**: both tables turn red and show "joined with". Click table 7: both are freed. |
| 1:55–2:30 | **Bad input** (35 s) | `@d@m`; size `9`; size `four`; Combine `5` + `5`; Combine `1` + `5`. Each shows a message, with no crash. |
| 2:30–3:00 | **Design** (30 s) | README prediction table: "Every class we said would stay the same did. The view keeps no copy of state, so the new feature only added reads." |

If we fall behind, skip two of the bad inputs. The Combine step is never cut.

### Who does what

| Item | Owner |
|---|---|
| Wait-time estimates in `Restaurant` / `Waitlist` | Zolo |
| Wait-time display in `TableTimeWindow` | Charlie |
| `HostControllerTest` with a fake window | Sieon |
| Tests for wait-time estimates; keep the full suite green | Sathvik |
| UI polish (disable Combine, list formatting) | Charlie |
| Final README and demo rehearsal | Everyone; Sathvik merges |

---

# Attribution

| Class / test file | Who worked on it | How |
|---|---|---|
| `LinkedQueue<T>` (+ `Node<T>`) | Zolo; Sathvik | Zolo wrote it. Sathvik added the `null` check and `toList()` in D3 (GenAI-assisted). Unchanged in D4. |
| `Party` | Zolo; Sathvik | Zolo wrote it. Sathvik added validation in D3 and raised the limit to 8 in D4 (GenAI-assisted). |
| `RestaurantListener` | Zolo | Wrote. Unchanged in D4. |
| `SizeGroup` | Sathvik | Wrote in D3; added `XLARGE` in D4 (GenAI-assisted) |
| `Table` | Sathvik | Wrote in D3; added joining in D4 (GenAI-assisted) |
| `Waitlist` | Sathvik | Wrote in D3; added the 7–8 queue and the `nextFor(8)` rule in D4 (GenAI-assisted) |
| `Restaurant` | Sathvik | Wrote in D3; added `seatCombined` and the combined `freeTable` in D4 (GenAI-assisted) |
| `TableTimeWindow` | Sathvik | Wrote in D3; added the Combine row and joined labels in D4 (GenAI-assisted) |
| `HostController` | Sieon; Sathvik | Sieon wrote it (GenAI-assisted). Sathvik fixed packages in D3, and added `handleCombine` and `parseWholeNumber` in D4 (GenAI-assisted). |
| `TableTimeApp` | Sieon; Sathvik | Sieon wrote it. Sathvik fixed `main` in D3. Unchanged in D4. |
| `LinkedQueueTest` | Sathvik | Wrote from the Test Plan (GenAI-assisted). In D4, existing tests unchanged; added 2 interleaved enqueue/dequeue tests (GenAI-assisted). |
| `PartyTest` | Sathvik | Wrote in D3; updated size limits in D4 (GenAI-assisted) |
| `SizeGroupTest` | Charlie; Sathvik | Charlie wrote it (normal and bad cases for `forSize` and `fitsAt`). Sathvik added the `XLARGE` cases and changed size 7 to 9 in D4 (GenAI-assisted). |
| `TableTest` | Charlie; Sathvik | Charlie wrote it (constructor, `seat`, `clear`, normal and bad cases). Sathvik added the `joinWith` and `unjoin` tests in D4 (GenAI-assisted). |
| `WaitlistTest` | Sathvik | Wrote in D3; added 4 Combine tests and 1 interleaved test in D4 (GenAI-assisted) |
| `RestaurantTest` | Sathvik | Wrote in D3; added 7 Combine tests and 1 duplicate-name test in D4 (GenAI-assisted) |
| `HostControllerInputTest` | Sathvik | New in D4 (GenAI-assisted) |
| `README.md` | Sieon; Sathvik | Sieon set up the template. Sathvik wrote the D3 and D4 versions (GenAI-assisted). |

## GenAI note

We used **Claude** for Deliverable 4 as follows:
- **Building the feature:** we asked it to implement Combine Tables following our D3 Next Steps prediction. We kept the prediction's design (an `XLARGE` group, `Restaurant.seatCombined`, and freeing either half frees both). Claude proposed two rules our prediction hadn't covered: capacity 8 only seats a 7–8 party, and only the 4-tops 5–7 can combine. We reviewed both and kept them.
- **Tests:** we asked for a normal and a bad-input test for every new or changed model method, interleaved queue tests, and a test for every bad-input case. That's how the parsing moved into `HostController.parseWholeNumber`, so it could be tested. We kept Charlie's own `TableTest` and `SizeGroupTest` and only added the Combine cases to them.
- **Checking the tests:** we had Claude break the code on purpose (for example, freeing only one half of a combined table) to confirm the tests catch real bugs. They did.
- **Bad-input run-through:** this is how we caught the stale "1 to 6" message in `HostController`, which we fixed.
- **README:** we asked it to draft this file. We checked the prediction-vs-actual table against the actual code changes, and the test count by running the tests.

The Deliverable 3 reflection and next steps are in this repo's history (commit `7ec3609`).
