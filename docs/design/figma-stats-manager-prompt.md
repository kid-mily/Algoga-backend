# 통계 매니저 (Analytics Admin) — Figma 생성용 마스터 프롬프트

> 아래 블록 전체를 Figma Make / Figma AI / v0 등 UI 생성 도구에 그대로 붙여넣으세요.
> 참고 이미지 4장(유저 유입 경로 / 나라별 인기도 / 예약 전환율 / 수강률)과 동일한 디자인 언어를 유지합니다.

---

## PROMPT START

Design a **Korean-language admin analytics dashboard** called **"Analytics Admin" (통계 매니저)** for a travel + e-learning service (algoga). It is a data-heavy, numbers-first internal tool for a "통계 매니저(statistics manager)" role. Produce a connected multi-screen Figma design (desktop web, 1440×1024) with a shared layout shell and one screen per menu item. Prioritize **clear numeric readouts, charts, and dense data tables** over decorative visuals.

### 0. Global shell (모든 화면 공통)

- **Left sidebar** (fixed, width 240px, white `#FFFFFF`, right border `#EEF0F2`):
  - Top: brand row = teal rounded-square logo mark (`#2DA98C`) + "Analytics Admin" (15px, semibold, `#1A1D1F`).
  - Nav menu items, each = 18px line icon + label (14px). Vertical spacing ~8px.
    1. 통계 대시보드
    2. 돈 요약 (매출 현황)
    3. 유저 유입 경로 분석
    4. 나라·강의 관심도 분석
    5. 강의·쿠폰 → 예약 전환
    6. 잔금·환불 관리
    7. 나라별 수익성 종합
  - **Active item**: mint background `#E9F7F2`, text/icon teal `#2DA98C`, 8px rounded, subtle left accent.
  - Bottom: "홈으로 나가기" link + user card (circular avatar, "김분석가", "analytics@algoga.kr" in 12px gray `#9AA0A6`).
- **Top bar** (height 56px, white): left close "✕" icon, center-left search input ("검색..." placeholder, rounded 8px, light gray fill `#F4F6F8`), right notification bell icon with red dot badge.
- **Content area**: background `#F4F6F8`, 32px padding.
  - Page header block: H1 title (26px, bold, `#1A1D1F`) + one-line subtitle (14px, `#8A9099`).
  - Filter/date row directly under header (see components).
  - Cards laid out in responsive grid.

### Design system (엄격히 준수)

- **Font**: Pretendard (fallback: system sans). Weights: Bold titles, SemiBold section heads, Regular body.
- **Type scale**: Page title 26 / section (card) title 16 semibold / body 14 / caption 12 / KPI number 28–32 bold.
- **Colors**
  - Primary teal `#2DA98C` (logo, active nav, primary bars, links, progress fill).
  - Categorical chart palette: teal `#2DA98C`, magenta `#E84E8A`, amber `#F2A93B`, purple `#8B7DF0`, gray `#B8BFC7`.
  - Positive/상승 green `#16B364`; Negative/하락 red `#F04438`.
  - Backgrounds: page `#F4F6F8`, card `#FFFFFF`.
  - Text: title `#1A1D1F`, body `#6B7280`, muted `#9AA0A6`.
  - Borders/dividers `#EEF0F2`.
- **Cards**: white, radius 14px, 1px border `#EEF0F2`, soft shadow (y2 blur8 rgba(16,24,40,0.04)), padding 20–24px. Section title top-left; a small **download (⬇) icon** top-right on chart cards.
- **Reusable components**
  - **KPI stat card**: small gray label (top) + big bold number + optional delta chip; pastel rounded-square icon badge top-right.
  - **Filter chips**: pill row; active = teal fill + white text, inactive = white fill, `#E5E8EB` border, gray text.
  - **Date range control**: preset pills (오늘 / 이번주 / 이번달 / 올해) + two date inputs (`YYYY-MM-DD`, calendar icon), aligned right.
  - **Donut/pie chart**: with % labels on slices + legend row below + download icon.
  - **Line chart**: smooth multi-series, light gridlines, date x-axis, legend below, download icon.
  - **Horizontal bar chart**: sorted desc, "Top N" dropdown selector (e.g. Top 10 / Top 5) top-right + download icon.
  - **Data table**: gray header row with sort arrows (↑↓), rows separated by `#F2F4F6` dividers; leading colored dot + label; numeric columns; **clickable numbers rendered in teal** (e.g. 예약 수); an inline **progress-bar + %** column for rates; row hover highlight. Footer: left "총 N개 · 1/2 페이지", right pagination (‹ 1 2 ›).
  - **CSV 내보내기 button**: outline button, `#E5E8EB` border, download icon + "CSV 내보내기", top-right of tables.
  - **Ranking list card**: "전환율 상위/하위 상품" style — rank items with title, small sub-metric line, big % on right (green for 상위/상승, red for 하위/하락), "더보기 (+N)" footer.

---

### Screen-by-screen (7 screens)

Use realistic Korean dummy data and 원 currency formatting (e.g. `182,400,000원`). Percentages to 1 decimal.

#### 1) 통계 대시보드 (landing hub)
- Row of 4 KPI stat cards: **순매출**, **미수금**, **환불율**, **잔금 전환율**.
- Wide **월별 순매출 추이** line chart.
- 2×2 mini "shortcut" widgets summarizing each analysis (유입 / 관심도 / 전환 / 잔금·환불) — each with 1 headline number and a "자세히 보기" link.

#### 2) 돈 요약 (매출 현황) — "현재 서비스 전체 매출 상태"
- KPI cards: **순매출**, **총매출**, **총환불액**, **미수금**, **환불율**, **잔금 전환율**.
- **월별 순매출 추이** line chart (총매출 vs 순매출 2 series).
- Monthly breakdown table: 월 | 총매출 | 환불액 | 순매출 | 미수금.

#### 3) 유저 유입 경로 분석 (참고 이미지 1과 동일 스타일)
- Filter chips: 전체 / 검색 엔진 / 소셜 미디어 / 지인 추천 / 광고 / 기타.
- Left: **유입 경로 비율** donut (검색엔진 43% / 소셜미디어 27% / 지인추천 15% / 광고 12% / 기타 3%).
- Right: **기간별 유입 추이** multi-line (경로별).
- Bottom table **유입 경로별 상세 통계** + search + CSV:
  columns = 유입 경로 | 회원가입 수 | 예약 수(teal, clickable) | **순매출(원)** | 전환율(progress bar).
  - NOTE: 순매출 컬럼은 신규 추가 지표.

#### 4) 나라·강의 관심도 분석 — "어떤 나라·강의에 관심을 보이는가" (참고 이미지 2 + 4 결합)
- Date range control (오늘/이번주/이번달/올해 + date pickers).
- **국가 섹션**: 좌 **국가별 예약 건수** (Top 10 horizontal bar, teal), 우 **국가별 매출** (Top 5 horizontal bar, amber). 아래 table: 국가 | 회원가입 수 | 예약 건수(clickable) | 매출(원) | 점유율(progress) + CSV + 국가명 검색 + pagination(총 20개 국가 · 1/2).
- **강의 섹션 (수강률)**: table 강의명 | 수강생 수 | 평균 진도율(progress) | 수료율(teal %) | 평균 학습 시간 + search + CSV + pagination(총 20개 강의).
  - Optional 컬럼 "조회 수" (관심도) — 데이터 있으면 표시, 없으면 생략.

#### 5) 강의·쿠폰 → 예약 전환 — "강의·쿠폰이 실제 예약을 만드는가" (참고 이미지 3 확장)
- Date presets (오늘/7일/30일/90일).
- **전환 KPI cards**: 결제 페이지 진입 수, 예약 완료 수, 예약 전환율.
- **기간별 예약 전환율** line chart + **상품별 전환율** horizontal bar (Top N).
- **전환율 상위 상품** / **전환율 하위 상품** two ranking list cards (green up / red down, "더보기 (+3)").
- **강의 전환 요약** row: 강의 수강자 예약 전환율, **완강자 예약 전환율**, 미완강 대비 배수(예: ×2.3), 수료율.
- **쿠폰 전환 섹션 (신규)**: KPI cards 쿠폰 발급 수, 쿠폰 사용률, 쿠폰 사용자 예약 전환율 + 쿠폰별 table (쿠폰명 | 발급 | 사용 | 사용률 | 예약 전환율 progress).
  - NOTE: 쿠폰 지표 3종은 신규 추가 대상.

#### 6) 잔금·환불 관리 — "예약된 매출이 실제 순매출로 남는가"
- **잔금 KPI cards**: 잔금 전환율, 미수금, D-day 임박 미납 건수, 위험(at-risk) 건수.
- **누적 납부율 곡선** line chart (계약금 납부 후 경과일 x축) + **국가별 잔금 전환율** bar.
- **미납 예약 목록** table + CSV: 예약번호 | 고객명 | 상품명 | 잔금액(원) | 계약금 납부일 | 경과일 | 체크인일 | D-day(빨강 강조).
- **환불 KPI cards**: 환불율, 순매출, 총환불액, 환불 건수, 평균 환불액.
- **환불 추이** line + **환불 시점 분석** bar + **국가별 환불** bar.
- **환불 사유** table (사유 | 건수 | 금액) + **취소 통계** card (취소 수, 취소율, 미납/계약금/완납 취소 breakdown, 결제후 취소율).

#### 7) 나라별 수익성 종합 — "결국 어떤 나라에 집중해야 하는가"
- **Summary KPI cards**: 분석 국가 수, 총 예약 건수, 총 순매출, 평균 환불율, 1위 국가(이름 + 점유율).
- **인기 vs 수익성 비교** chart: bubble/scatter (x=예약 수, y=순매출, 버블 크기=점유율) 또는 dual bar.
- **국가별 수익성 종합** table + CSV, sortable:
  국가 | 예약 수 | 총매출 | **순매출** | 환불율 | 잔금 전환율 | 취소율 | 강의 전환율 | 쿠폰 전환율 | 점유율(progress).
  - 투자 대상/개선 대상 국가는 행 배경 또는 태그로 하이라이트.

### Cross-screen behavior
- 화면 간 연결 흐름: 돈 요약(이상 감지) → 유입 → 관심도 → 전환 → 잔금·환불 → 국가 수익성(최종 의사결정). 각 화면 상단 breadcrumb 또는 이전/다음 흐름 힌트를 얇게 표시.
- 모든 표의 rate 컬럼은 progress-bar + % 조합, 모든 화면 우상단에 기간 컨트롤 일관 배치.

## PROMPT END
