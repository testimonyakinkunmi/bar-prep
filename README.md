# BarPrep NG — Civil Litigation Quiz App

Offline Android quiz app for Nigerian Law School Bar Final preparation.  
**Subject: Civil Litigation — Weeks 3–19 (17 topics, 450+ questions)**

---

## Features

- **17 topic-by-topic weeks** — pick any week and quiz on it
- **Random Mix mode** — 20 questions pulled from all weeks
- **Immediate grading** — answer revealed the moment you submit
- **Detailed corrections** — explanation + law reference for every question
- **Streak tracking** — consecutive days of study
- **Score over time chart** — custom-drawn line graph per topic or all
- **Topic heatmap** — colour-coded accuracy grid across all 17 weeks
- **Hardest questions list** — questions you struggle with most
- **Weak areas drill** — auto-builds a session from your worst topics
- **Quick recall micro-quiz** — 4-question widget on the home screen
- **100% offline** — no internet required after install

---

## Build Requirements

| Tool | Version |
|------|---------|
| JDK | 17+ |
| Android Studio | Hedgehog+ (optional) |
| Android SDK | API 26+ |

---

## Quick Start

```bash
# 1. Clone or extract this folder
cd BarPrepNG

# 2. Build
./build.sh

# 3. Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The build script auto-downloads:
- Gradle 8.2
- All Android dependencies (Material, AppCompat, Gson, etc.)
- Inter font family (OFL license)

---

## Project Structure

```
app/src/main/
├── java/com/barprepng/app/
│   ├── MainActivity.kt              — Nav host + fragment management
│   ├── data/
│   │   ├── Models.kt               — All data classes
│   │   ├── DatabaseHelper.kt       — SQLite (attempts, streaks, accuracy)
│   │   └── QuizRepository.kt       — Loads JSON, builds sessions, saves results
│   └── ui/
│       ├── home/
│       │   ├── HomeFragment.kt     — Week picker + micro-quiz widget
│       │   └── WeekAdapter.kt      — RecyclerView for week list
│       ├── quiz/
│       │   └── QuizFragment.kt     — Full quiz flow with reveal logic
│       ├── results/
│       │   ├── ResultsFragment.kt  — Score + per-question review
│       │   └── ReviewAdapter.kt    — Review list adapter
│       ├── progress/
│       │   └── ProgressFragment.kt — Per-week stats + recent attempts
│       └── insights/
│           ├── InsightsFragment.kt — 4-tab dashboard (toggle)
│           ├── LineChartView.kt    — Custom canvas line chart
│           └── HeatmapView.kt      — Custom canvas heatmap grid
└── res/
    ├── raw/quiz_data.json          — All 450+ questions (17 weeks)
    ├── layout/                     — All XML layouts
    ├── drawable/                   — Vector icons + shape backgrounds
    ├── values/                     — Colors, strings, themes, dimens
    └── anim/                       — Fade transitions
```

---

## Quiz Data Schema

```json
{
  "subject": "Civil Litigation",
  "weeks": [
    {
      "week_number": 3,
      "title": "Courts with Civil Jurisdiction",
      "questions": [
        {
          "id": "CIV_W3_Q1",
          "week": 3,
          "topic": "Courts with Civil Jurisdiction",
          "scenario": "Optional scenario text shown above question...",
          "question": "Which court would have the jurisdiction...",
          "options": ["The Supreme Court", "The Court of Appeal", "..."],
          "correct_index": 0,
          "explanation": "Dispute between the National Assembly and...",
          "law_reference": "Sec. 1, Supreme Court (Additional Original Jurisdiction) Act, 2002"
        }
      ]
    }
  ]
}
```

---

## Adding More Questions

Edit `app/src/main/res/raw/quiz_data.json` — the schema is self-documenting.  
IDs follow the pattern `CIV_W{week}_{Q{n}` — keep them unique.

---

## Adding More Subjects (Criminal Litigation, etc.)

1. Create `res/raw/criminal_quiz_data.json` with the same schema
2. In `QuizRepository`, add a second `SubjectData` loader
3. Add a subject selector on the Home screen

