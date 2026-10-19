"""Reproduce descriptive SRC7 wave metrics without inferring fatigue or readiness."""

import argparse
import json
from pathlib import Path


PRIMARY = ("Присед", "Жим лежа", "Становая тяга")


def analyze(plan):
    result = []
    for cycle in sorted({day["cycle"] for day in plan["days"]}):
        days = [day for day in plan["days"] if day["cycle"] == cycle]
        exercises = [exercise for day in days for exercise in day["exercises"]]
        lifts = {}
        for name in PRIMARY:
            groups = [(exercise["factor"] * group["percent"], group["sets"] * group["reps"])
                      for exercise in exercises if exercise["name"] == name
                      for group in exercise["groups"]]
            reps = sum(count for _, count in groups)
            lifts[name] = {
                "repetitions": reps,
                "repetition_weighted_mean_reference_percent": round(
                    100 * sum(intensity * count for intensity, count in groups) / reps, 2
                ) if reps else None,
                "peak_reference_percent": round(100 * max((value for value, _ in groups), default=0), 2),
            }
        result.append({
            "cycle": cycle,
            "populated_days": len(days),
            "source_day_numbers": [day["day"] for day in days],
            "prescribed_sets_including_ramps": sum(group["sets"] for exercise in exercises for group in exercise["groups"]),
            "prescribed_repetitions": sum(group["sets"] * group["reps"] for exercise in exercises for group in exercise["groups"]),
            "exercise_row_labels": {label: sum(exercise["load"] == label for exercise in exercises)
                                    for label in ("Тяжелая", "Средняя", "Легкая")},
            "primary_lifts_only": lifts,
        })
    return {
        "schema_version": 1,
        "source": "app/src/main/assets/workout-plan.json",
        "scope": "all 57 populated days; no actual training performance or calendar dates inferred",
        "method": "Reference intensity = exercise.factor * group.percent before weight rounding. "
                  "Means are weighted by prescribed repetitions. Sets include ramp sets. "
                  "Labels describe individual exercise rows, not whole days. Cycles have unequal populated-day counts.",
        "limitations": "Descriptive plan metrics, not fatigue, injury risk, recovery, hard-set equivalence "
                       "or observed training. Do not treat incomplete cycles as confirmed deloads.",
        "cycles": result,
    }


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--out", type=Path)
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    report = analyze(json.loads((root / "app/src/main/assets/workout-plan.json").read_text(encoding="utf-8")))
    text = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    if args.out:
        args.out.write_text(text, encoding="utf-8")
    else:
        print(text)
