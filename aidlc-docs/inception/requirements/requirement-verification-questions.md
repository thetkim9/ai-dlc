# 요구사항 검증 질문 (Requirements Verification Questions)

`requirements/table-order-requirements.md` 문서를 분석했습니다.
아래 질문들에 답변해 주세요. 각 질문의 `[Answer]:` 태그 뒤에 선택한 알파벳을 입력해 주세요.
"Other"를 선택하신 경우, 알파벳 뒤에 직접 설명을 추가해 주세요.

---

## Question 1
백엔드 기술 스택을 어떻게 구성하시겠습니까?

A) Node.js + Express (JavaScript/TypeScript)
B) Python + FastAPI
C) Java + Spring Boot
D) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 2
프론트엔드 기술 스택을 어떻게 구성하시겠습니까?

A) React (TypeScript)
B) Vue.js
C) Vanilla JavaScript (프레임워크 없음)
D) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 3
데이터베이스를 어떻게 구성하시겠습니까?

A) PostgreSQL (관계형 DB)
B) MySQL (관계형 DB)
C) SQLite (경량 관계형 DB, 개발/소규모용)
D) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 4
배포 환경은 어떻게 구성하시겠습니까?

A) AWS (EC2, RDS 등)
B) Docker + 로컬 서버
C) 단순 로컬 개발 환경 (배포 고려 없음)
D) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 5
메뉴 이미지 저장 방식을 어떻게 하시겠습니까?

A) 외부 URL 입력 방식 (이미지 직접 업로드 없음)
B) 서버에 직접 파일 업로드
C) AWS S3 또는 외부 스토리지 서비스 사용
D) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 6
요구사항 문서에 메뉴 관리(등록/수정/삭제)가 포함되어 있지만 MVP 범위에는 명시되지 않았습니다. 메뉴 관리 기능을 이번 구현에 포함하시겠습니까?

A) 예, 메뉴 관리 기능도 포함 (CRUD 전체)
B) 아니오, MVP 범위만 구현 (메뉴 관리 제외, 초기 데이터는 DB seed로 처리)
C) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 7
실시간 주문 업데이트 방식으로 요구사항에 SSE(Server-Sent Events)가 명시되어 있습니다. 이를 그대로 사용하시겠습니까?

A) 예, SSE 사용 (요구사항 그대로)
B) WebSocket으로 변경
C) Polling 방식으로 변경 (단순 구현)
D) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 8
주문 상태 실시간 업데이트(고객 화면에서 주문 상태 변경 반영)를 구현하시겠습니까?
요구사항 문서에 "(선택사항)"으로 표시되어 있습니다.

A) 예, 구현 (고객 화면에서도 SSE로 실시간 상태 업데이트)
B) 아니오, 제외 (고객은 수동 새로고침으로 확인)
C) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 9
모노레포(monorepo) 구조로 프론트엔드와 백엔드를 하나의 저장소에서 관리하시겠습니까?

A) 예, 모노레포 (frontend/, backend/ 폴더로 분리)
B) 아니오, 별도 저장소 (현재는 하나의 저장소에 백엔드만)
C) 풀스택 프레임워크 사용 (Next.js 등)
D) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 10
테이블 수 및 초기 데이터 규모는 어느 정도로 예상하시나요?

A) 소규모 (테이블 1~10개, 메뉴 10~30개)
B) 중규모 (테이블 10~30개, 메뉴 30~100개)
C) 대규모 (테이블 30개 이상, 메뉴 100개 이상)
D) Other (please describe after [Answer]: tag below)

[Answer]: A

---

모든 질문에 답변 완료 후 "완료" 또는 "done"이라고 알려주세요.
