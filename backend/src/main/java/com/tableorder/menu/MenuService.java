package com.tableorder.menu;

import com.tableorder.common.exception.ApiException;
import com.tableorder.entity.Category;
import com.tableorder.entity.Menu;
import com.tableorder.entity.Store;
import com.tableorder.menu.dto.*;
import com.tableorder.repository.CategoryRepository;
import com.tableorder.repository.MenuRepository;
import com.tableorder.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MenuService {

    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final FileStorageService fileStorageService;

    public MenuService(StoreRepository storeRepository,
                       CategoryRepository categoryRepository,
                       MenuRepository menuRepository,
                       FileStorageService fileStorageService) {
        this.storeRepository = storeRepository;
        this.categoryRepository = categoryRepository;
        this.menuRepository = menuRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<CategoryResponse> getCategoriesByStore(Long storeId) {
        Store store = getStore(storeId);
        return categoryRepository.findAllByStoreOrderByDisplayOrder(store)
                .stream().map(CategoryResponse::new).collect(Collectors.toList());
    }

    public List<MenuResponse> getMenusByCategory(Long storeId, Long categoryId) {
        Store store = getStore(storeId);
        Category category = getCategory(categoryId);
        return menuRepository.findByStoreAndCategoryOrderByDisplayOrder(store, category)
                .stream().map(MenuResponse::new).collect(Collectors.toList());
    }

    @Transactional
    public MenuResponse createMenu(Long storeId, MenuCreateRequest request, MultipartFile imageFile) {
        Store store = getStore(storeId);
        Category category = getCategory(request.getCategoryId());

        // 서비스 레벨 검증
        if (request.getName() == null || request.getName().isBlank()) {
            throw ApiException.badRequest("메뉴명은 필수입니다.");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw ApiException.badRequest("가격은 0보다 커야 합니다.");
        }

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = fileStorageService.saveFile(storeId, imageFile);
        }

        int nextOrder = menuRepository.countByCategory(category);

        Menu menu = new Menu();
        menu.setStore(store);
        menu.setCategory(category);
        menu.setName(request.getName());
        menu.setPrice(request.getPrice());
        menu.setDescription(request.getDescription());
        menu.setImageUrl(imageUrl);
        menu.setDisplayOrder(nextOrder + 1);
        menu.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);

        return new MenuResponse(menuRepository.save(menu));
    }

    @Transactional
    public MenuResponse updateMenu(Long storeId, Long menuId, MenuUpdateRequest request, MultipartFile imageFile) {
        Store store = getStore(storeId);
        Menu menu = menuRepository.findByIdAndStore(menuId, store)
                .orElseThrow(() -> ApiException.notFound("메뉴를 찾을 수 없습니다."));

        if (request.getName() != null && !request.getName().isBlank()) {
            menu.setName(request.getName());
        }
        if (request.getPrice() != null) {
            if (request.getPrice() <= 0) throw ApiException.badRequest("가격은 0보다 커야 합니다.");
            menu.setPrice(request.getPrice());
        }
        if (request.getCategoryId() != null) {
            menu.setCategory(getCategory(request.getCategoryId()));
        }
        if (request.getDescription() != null) {
            menu.setDescription(request.getDescription());
        }
        if (request.getAvailable() != null) {
            menu.setAvailable(request.getAvailable());
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            if (menu.getImageUrl() != null) fileStorageService.deleteFile(menu.getImageUrl());
            menu.setImageUrl(fileStorageService.saveFile(storeId, imageFile));
        }
        menu.setUpdatedAt(LocalDateTime.now());

        return new MenuResponse(menuRepository.save(menu));
    }

    @Transactional
    public void deleteMenu(Long storeId, Long menuId) {
        Store store = getStore(storeId);
        Menu menu = menuRepository.findByIdAndStore(menuId, store)
                .orElseThrow(() -> ApiException.notFound("메뉴를 찾을 수 없습니다."));
        if (menu.getImageUrl() != null) fileStorageService.deleteFile(menu.getImageUrl());
        menuRepository.delete(menu);
    }

    @Transactional
    public void updateMenuOrder(Long storeId, List<MenuOrderRequest> orderList) {
        Store store = getStore(storeId);
        List<Long> ids = orderList.stream().map(MenuOrderRequest::getMenuId).collect(Collectors.toList());
        List<Menu> menus = menuRepository.findAllByIdInAndStore(ids, store);

        menus.forEach(menu -> orderList.stream()
                .filter(o -> o.getMenuId().equals(menu.getId()))
                .findFirst()
                .ifPresent(o -> menu.setDisplayOrder(o.getDisplayOrder())));

        menuRepository.saveAll(menus);
    }

    private Store getStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> ApiException.notFound("매장을 찾을 수 없습니다."));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> ApiException.notFound("카테고리를 찾을 수 없습니다."));
    }
}
