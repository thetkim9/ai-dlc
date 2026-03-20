package com.tableorder.menu;

import com.tableorder.common.exception.ApiException;
import com.tableorder.entity.*;
import com.tableorder.menu.dto.*;
import com.tableorder.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock StoreRepository storeRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock MenuRepository menuRepository;
    @Mock FileStorageService fileStorageService;

    @InjectMocks MenuService menuService;

    private Store store;
    private Category category;
    private Menu menu;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);
        store.setStoreCode("STORE001");

        category = new Category();
        category.setId(10L);
        category.setStore(store);
        category.setName("메인 메뉴");
        category.setDisplayOrder(1);

        menu = new Menu();
        menu.setId(100L);
        menu.setStore(store);
        menu.setCategory(category);
        menu.setName("불고기 버거");
        menu.setPrice(8900);
        menu.setDisplayOrder(1);
        menu.setAvailable(true);
    }

    // TC-BE-007: 메뉴 등록 성공
    @Test
    @DisplayName("TC-BE-007: 메뉴 등록 성공")
    void createMenu_validRequest_success() {
        // given
        MenuCreateRequest request = new MenuCreateRequest();
        setField(request, "name", "불고기 버거");
        setField(request, "price", 8900);
        setField(request, "categoryId", 10L);

        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(categoryRepository.findById(10L)).willReturn(Optional.of(category));
        given(menuRepository.countByCategory(category)).willReturn(0);
        given(menuRepository.save(any(Menu.class))).willReturn(menu);

        // when
        MenuResponse response = menuService.createMenu(1L, request, null);

        // then
        assertThat(response.getName()).isEqualTo("불고기 버거");
        assertThat(response.getPrice()).isEqualTo(8900);
        then(menuRepository).should().save(any(Menu.class));
    }

    // TC-BE-008: 가격 0 이하
    @Test
    @DisplayName("TC-BE-008: 가격 0 이하 메뉴 등록 실패 - 400")
    void createMenu_zeroPriceViaAnnotation_throws400() {
        // given - price=0 은 @Min(1) 으로 Bean Validation에서 처리되지만
        // Service 레벨에서도 방어적으로 검증
        MenuCreateRequest request = new MenuCreateRequest();
        setField(request, "name", "테스트");
        setField(request, "price", 0);
        setField(request, "categoryId", 10L);

        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(categoryRepository.findById(10L)).willReturn(Optional.of(category));

        // when & then
        assertThatThrownBy(() -> menuService.createMenu(1L, request, null))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // TC-BE-009: 메뉴명 누락
    @Test
    @DisplayName("TC-BE-009: 메뉴명 누락 시 실패 - 400")
    void createMenu_blankName_throws400() {
        MenuCreateRequest request = new MenuCreateRequest();
        setField(request, "name", "");
        setField(request, "price", 8900);
        setField(request, "categoryId", 10L);

        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(categoryRepository.findById(10L)).willReturn(Optional.of(category));

        assertThatThrownBy(() -> menuService.createMenu(1L, request, null))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // TC-BE-010: 메뉴 삭제 성공
    @Test
    @DisplayName("TC-BE-010: 메뉴 삭제 성공")
    void deleteMenu_exists_success() {
        // given
        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(menuRepository.findByIdAndStore(100L, store)).willReturn(Optional.of(menu));

        // when
        menuService.deleteMenu(1L, 100L);

        // then
        then(menuRepository).should().delete(menu);
    }

    // TC-BE-011: 존재하지 않는 메뉴 삭제
    @Test
    @DisplayName("TC-BE-011: 존재하지 않는 메뉴 삭제 - 404")
    void deleteMenu_notFound_throws404() {
        // given
        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(menuRepository.findByIdAndStore(999L, store)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> menuService.deleteMenu(1L, 999L))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getCategoriesByStore_success() {
        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(categoryRepository.findAllByStoreOrderByDisplayOrder(store)).willReturn(List.of(category));

        List<CategoryResponse> result = menuService.getCategoriesByStore(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("메인 메뉴");
    }

    @Test
    @DisplayName("카테고리별 메뉴 목록 조회 성공")
    void getMenusByCategory_success() {
        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(categoryRepository.findById(10L)).willReturn(Optional.of(category));
        given(menuRepository.findByStoreAndCategoryOrderByDisplayOrder(store, category))
            .willReturn(List.of(menu));

        List<MenuResponse> result = menuService.getMenusByCategory(1L, 10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("불고기 버거");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
