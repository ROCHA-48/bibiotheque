#!/usr/bin/env python3
"""Génère un rapport HTML visuel des tests backend (surefire) pour la démo."""
import glob
import os
import xml.etree.ElementTree as ET

REPORTS_DIR = os.path.join(os.path.dirname(__file__), "target", "surefire-reports")
OUTPUT = os.path.join(os.path.dirname(__file__), "target", "test-report.html")

rows = []
total_tests = total_failures = total_errors = total_skipped = 0

for xml_file in sorted(glob.glob(os.path.join(REPORTS_DIR, "TEST-*.xml"))):
    tree = ET.parse(xml_file)
    suite = tree.getroot()
    name = suite.get("name", "?")
    tests = int(suite.get("tests", 0))
    failures = int(suite.get("failures", 0))
    errors = int(suite.get("errors", 0))
    skipped = int(suite.get("skipped", 0))
    time_s = suite.get("time", "0")

    total_tests += tests
    total_failures += failures
    total_errors += errors
    total_skipped += skipped

    ok = failures == 0 and errors == 0
    badge = '<span class="badge ok">✅ PASSED</span>' if ok else '<span class="badge ko">❌ FAILED</span>'

    case_rows = ""
    for case in suite.iter("testcase"):
        cname = case.get("name", "?")
        ctime = case.get("time", "0")
        failed = case.find("failure") is not None or case.find("error") is not None
        status = ('<span class="case ko">❌</span>' if failed
                  else '<span class="case ok">✅</span>')
        case_rows += f'<tr><td>{status}</td><td class="mono">{cname}</td><td>{float(ctime):.3f}s</td></tr>'

    rows.append(f"""
    <div class="suite">
      <div class="suite-header">
        <div><h2>{name.split('.')[-1]}</h2><p class="pkg">{name}</p></div>
        {badge}
      </div>
      <p class="stats">{tests} tests · {failures} échecs · {errors} erreurs · {skipped} ignorés · {float(time_s):1f}s</p>
      <table>{case_rows}</table>
    </div>""")

all_ok = total_failures == 0 and total_errors == 0
summary_class = "ok" if all_ok else "ko"
summary_text = "✅ TOUS LES TESTS PASSENT" if all_ok else "❌ DES TESTS ÉCHOUENT"

html = f"""<!DOCTYPE html>
<html lang="fr">
<head>
<meta charset="UTF-8">
<title>Rapport de tests — Bibliothèque</title>
<style>
  body {{ font-family: -apple-system, 'Segoe UI', Roboto, sans-serif; background:#f1f5f9; margin:0; padding:2rem; }}
  .container {{ max-width: 900px; margin: 0 auto; }}
  h1 {{ color:#0f172a; }}
  .summary {{ padding:1.2rem 1.5rem; border-radius:12px; color:white; font-size:1.2rem; font-weight:700;
              margin-bottom:2rem; }}
  .summary.ok {{ background: linear-gradient(135deg,#16a34a,#15803d); }}
  .summary.ko {{ background: linear-gradient(135deg,#dc2626,#b91c1c); }}
  .summary small {{ display:block; font-weight:400; opacity:.9; margin-top:.3rem; }}
  .suite {{ background:white; border-radius:12px; padding:1.2rem 1.5rem; margin-bottom:1.5rem;
            box-shadow:0 1px 3px rgba(0,0,0,.08); }}
  .suite-header {{ display:flex; justify-content:space-between; align-items:center; }}
  .suite h2 {{ margin:0; color:#0f172a; font-size:1.15rem; }}
  .pkg {{ margin:.2rem 0 0; color:#64748b; font-size:.8rem; }}
  .badge {{ padding:.35rem .8rem; border-radius:999px; font-size:.8rem; font-weight:700; }}
  .badge.ok {{ background:#dcfce7; color:#15803d; }}
  .badge.ko {{ background:#fee2e2; color:#b91c1c; }}
  .stats {{ color:#475569; font-size:.85rem; margin:.6rem 0 1rem; }}
  table {{ width:100%; border-collapse:collapse; }}
  td {{ padding:.45rem .5rem; border-top:1px solid #f1f5f9; font-size:.88rem; }}
  td.mono {{ font-family:ui-monospace,Consolas,monospace; font-size:.82rem; color:#334155; }}
  .case.ok {{ color:#16a34a; }} .case.ko {{ color:#dc2626; }}
  footer {{ text-align:center; color:#94a3b8; font-size:.8rem; margin-top:2rem; }}
</style>
</head>
<body>
<div class="container">
  <h1>🧪 Rapport de tests — Module Réservation</h1>
  <div class="summary {summary_class}">{summary_text}
    <small>{total_tests} tests · {total_failures} échecs · {total_errors} erreurs · {total_skipped} ignorés</small>
  </div>
  {''.join(rows)}
  <footer>Généré depuis target/surefire-reports — mvn test · {os.path.basename(OUTPUT)}</footer>
</div>
</body>
</html>"""

with open(OUTPUT, "w", encoding="utf-8") as f:
    f.write(html)
print(f"Rapport généré : {OUTPUT}")
