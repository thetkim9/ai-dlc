# Unit of Work Story Map - 테이블오더 서비스

## Story → Unit 매핑

| Story ID | Story 제목 | Unit | 우선순위 |
|---------|-----------|------|---------|
| C1-1 | 테이블 자동 로그인 | Unit 1 (Backend) + Unit 2 (Customer) | Must Have |
| C2-1 | 카테고리별 메뉴 조회 | Unit 1 (Backend) + Unit 2 (Customer) | Must Have |
| C2-2 | 메뉴 상세 정보 확인 | Unit 2 (Customer) | Should Have |
| C3-1 | 장바구니 메뉴 추가/수량 조절 | Unit 2 (Customer) | Must Have |
| C3-2 | 장바구니 비우기 | Unit 2 (Customer) | Should Have |
| C4-1 | 주문 확정 | Unit 1 (Backend) + Unit 2 (Customer) | Must Have |
| C5-1 | 현재 세션 주문 내역 조회 | Unit 1 (Backend) + Unit 2 (Customer) | Must Have |
| C5-2 | 주문 상태 실시간 확인 | Unit 1 (Backend) + Unit 2 (Customer) | Must Have |
| A1-1 | 관리자 로그인 | Unit 1 (Backend) + Unit 3 (Admin) | Must Have |
| A2-1 | 테이블별 주문 대시보드 조회 | Unit 1 (Backend) + Unit 3 (Admin) | Must Have |
| A2-2 | 주문 상태 변경 | Unit 1 (Backend) + Unit 3 (Admin) | Must Have |
| A3-1 | 테이블 초기 설정 | Unit 1 (Backend) + Unit 3 (Admin) | Must Have |
| A3-2 | 주문 삭제 (직권 수정) | Unit 1 (Backend) + Unit 3 (Admin) | Must Have |
| A3-3 | 테이블 이용 완료 처리 | Unit 1 (Backend) + Unit 3 (Admin) | Must Have |
| A3-4 | 과거 주문 내역 조회 | Unit 1 (Backend) + Unit 3 (Admin) | Should Have |
| A4-1 | 메뉴 등록 | Unit 1 (Backend) + Unit 3 (Admin) | Must Have |
| A4-2 | 메뉴 수정 및 삭제 | Unit 1 (Backend) + Unit 3 (Admin) | Must Have |
| A4-3 | 메뉴 노출 순서 조정 | Unit 1 (Backend) + Unit 3 (Admin) | Could Have |

---

## Unit별 Story 요약

### Unit 1: Backend (18개 Story 모두 관련)
백엔드는 모든 Story의 API를 제공하므로 전체 Story와 연관됩니다.

**핵심 구현 항목**:
- 테이블 인증 API (C1-1)
- 메뉴/카테고리 조회 API (C2-1)
- 주문 생성/조회 API (C4-1, C5-1)
- SSE 스트림 - 고객용 (C5-2), 관리자용 (A2-1)
- 관리자 인증 API (A1-1)
- 주문 상태 변경 API (A2-2)
- 테이블 관리 API (A3-1, A3-2, A3-3, A3-4)
- 메뉴 관리 CRUD API (A4-1, A4-2, A4-3)

### Unit 2: Frontend - Customer (8개 Story)
C1-1, C2-1, C2-2, C3-1, C3-2, C4-1, C5-1, C5-2

**Must Have**: C1-1, C2-1, C3-1, C4-1, C5-1, C5-2 (6개)
**Should Have**: C2-2, C3-2 (2개)

### Unit 3: Frontend - Admin (10개 Story)
A1-1, A2-1, A2-2, A3-1, A3-2, A3-3, A3-4, A4-1, A4-2, A4-3

**Must Have**: A1-1, A2-1, A2-2, A3-1, A3-2, A3-3, A4-1, A4-2 (8개)
**Should Have**: A3-4 (1개)
**Could Have**: A4-3 (1개)

---

## Must Have Story 커버리지 확인

| Must Have Story | Unit 1 | Unit 2 | Unit 3 |
|----------------|--------|--------|--------|
| C1-1 자동 로그인 | API | UI | - |
| C2-1 메뉴 조회 | API | UI | - |
| C3-1 장바구니 | - | UI | - |
| C4-1 주문 확정 | API | UI | - |
| C5-1 주문 내역 | API | UI | - |
| C5-2 실시간 상태 | SSE | Hook | - |
| A1-1 관리자 로그인 | API | - | UI |
| A2-1 대시보드 | API+SSE | - | UI |
| A2-2 상태 변경 | API | - | UI |
| A3-1 테이블 설정 | API | - | UI |
| A3-2 주문 삭제 | API | - | UI |
| A3-3 이용 완료 | API | - | UI |
| A4-1 메뉴 등록 | API | - | UI |
| A4-2 메뉴 수정/삭제 | API | - | UI |

모든 Must Have Story가 Unit에 할당되었습니다. ✓
