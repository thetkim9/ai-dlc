# Execution Plan - 테이블오더 서비스

## Detailed Analysis Summary

### Change Impact Assessment
- **User-facing changes**: Yes - 고객 주문 UI, 관리자 대시보드 신규 구축
- **Structural changes**: Yes - 모노레포 구조, 백엔드/프론트엔드 분리
- **Data model changes**: Yes - Store, Table, TableSession, Menu, Order 등 신규 스키마
- **API changes**: Yes - REST API + SSE endpoint 신규 설계
- **NFR impact**: Yes - JWT 인증, bcrypt, SSE 실시간 통신, Docker 배포

### Risk Assessment
- **Risk Level**: Medium
- **Rollback Complexity**: Easy (Greenfield, 기존 시스템 없음)
- **Testing Complexity**: Moderate (SSE 실시간 통신, 세션 관리 테스트 필요)

---

## Workflow Visualization

```
INCEPTION PHASE
+---------------------------+
| Workspace Detection  DONE |
| Requirements Analysis DONE|
| User Stories         DONE |
| Workflow Planning    NOW  |
| Application Design   NEXT |
| Units Generation     NEXT |
+---------------------------+
            |
            v
CONSTRUCTION PHASE (per unit)
+---------------------------+
| Functional Design  EXECUTE|
| NFR Requirements   EXECUTE|
| NFR Design         EXECUTE|
| Infrastructure     EXECUTE|
| Code Generation    EXECUTE|
+---------------------------+
            |
            v
+---------------------------+
| Build and Test     EXECUTE|
+---------------------------+
```

---

## Phases to Execute

### INCEPTION PHASE
- [x] Workspace Detection - COMPLETED
- [x] Requirements Analysis - COMPLETED
- [x] User Stories - COMPLETED
- [x] Workflow Planning - IN PROGRESS
- [ ] Application Design - EXECUTE
  - **Rationale**: 신규 시스템으로 백엔드 레이어 구조(Controller/Service/Repository), React 컴포넌트 구조, API 계약 설계가 필요. 컴포넌트 경계와 의존성을 사전에 정의해야 코드 생성 품질이 높아짐.
- [ ] Units Generation - EXECUTE
  - **Rationale**: 백엔드(Spring Boot), 프론트엔드-고객(React), 프론트엔드-관리자(React), 인프라(Docker) 4개 Unit으로 분리하여 각각 독립적으로 설계 및 구현 가능.

### CONSTRUCTION PHASE (Unit별 반복)

#### Unit 1: Backend (Spring Boot)
- [ ] Functional Design - EXECUTE
  - **Rationale**: DB 스키마, API 엔드포인트, 비즈니스 로직(세션 관리, 주문 처리) 상세 설계 필요
- [ ] NFR Requirements - EXECUTE
  - **Rationale**: JWT 인증, bcrypt, SSE, 로그인 시도 제한 등 보안/성능 NFR 존재
- [ ] NFR Design - EXECUTE
  - **Rationale**: Spring Security 설정, SSE 구현 패턴, 파일 업로드 처리 설계 필요
- [ ] Infrastructure Design - EXECUTE
  - **Rationale**: Docker 컨테이너 구성, PostgreSQL 연결, 파일 볼륨 마운트 설계 필요
- [ ] Code Generation - EXECUTE

#### Unit 2: Frontend - Customer (React)
- [ ] Functional Design - EXECUTE
  - **Rationale**: 고객 UI 컴포넌트 구조, 상태 관리(장바구니, SSE), 라우팅 설계 필요
- [ ] NFR Requirements - EXECUTE
  - **Rationale**: 터치 친화적 UI, SSE 연결 관리, localStorage 보안 고려 필요
- [ ] NFR Design - EXECUTE
  - **Rationale**: SSE hook 설계, localStorage 관리 패턴, 에러 처리 UX 설계 필요
- [ ] Infrastructure Design - SKIP
  - **Rationale**: 프론트엔드는 백엔드 Docker 설정에 포함되거나 정적 파일 서빙으로 처리. 별도 인프라 설계 불필요.
- [ ] Code Generation - EXECUTE

#### Unit 3: Frontend - Admin (React)
- [ ] Functional Design - EXECUTE
  - **Rationale**: 관리자 대시보드 컴포넌트 구조, 실시간 그리드 레이아웃, 메뉴 CRUD UI 설계 필요
- [ ] NFR Requirements - EXECUTE
  - **Rationale**: JWT 토큰 관리, SSE 대시보드 업데이트, 이미지 업로드 UX 고려 필요
- [ ] NFR Design - EXECUTE
  - **Rationale**: JWT interceptor 설계, SSE 재연결 전략, 파일 업로드 컴포넌트 설계 필요
- [ ] Infrastructure Design - SKIP
  - **Rationale**: Unit 2와 동일한 이유. 프론트엔드 인프라는 백엔드 설정에 통합.
- [ ] Code Generation - EXECUTE

#### Build and Test (전체 완료 후)
- [ ] Build and Test - EXECUTE
  - **Rationale**: Docker Compose 통합 빌드, 단위 테스트, 통합 테스트(SSE, 주문 플로우) 필요

---

## Unit 구성 요약

| Unit | 기술 | 주요 내용 |
|------|------|-----------|
| Unit 1: Backend | Java + Spring Boot + PostgreSQL | API, 인증, SSE, 파일 업로드, DB |
| Unit 2: Frontend-Customer | React (TypeScript) | 메뉴 조회, 장바구니, 주문, SSE 상태 |
| Unit 3: Frontend-Admin | React (TypeScript) | 대시보드, 테이블 관리, 메뉴 CRUD |

---

## Success Criteria
- **Primary Goal**: 고객이 테이블에서 주문하고 관리자가 실시간으로 확인/관리할 수 있는 시스템
- **Key Deliverables**:
  - Docker Compose로 실행 가능한 전체 스택
  - 고객용 React 앱 (자동 로그인, 메뉴, 장바구니, 주문)
  - 관리자용 React 앱 (대시보드, 테이블 관리, 메뉴 CRUD)
  - Spring Boot REST API + SSE
  - PostgreSQL DB 스키마 및 초기 데이터
- **Quality Gates**:
  - 모든 Must Have Story Acceptance Criteria 충족
  - SSE 실시간 업데이트 2초 이내 동작
  - JWT 인증 및 세션 관리 정상 동작
  - Docker Compose up 명령으로 전체 스택 실행 가능
