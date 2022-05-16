package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Menu;
import com.mycompany.myapp.repository.MenuRepository;
import com.mycompany.myapp.service.MenuService;
import com.mycompany.myapp.service.dto.MenuDTO;
import com.mycompany.myapp.service.mapper.MenuMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Menu}.
 */
@Service
@Transactional
public class MenuServiceImpl implements MenuService {

    private final Logger log = LoggerFactory.getLogger(MenuServiceImpl.class);

    private final MenuRepository menuRepository;

    private final MenuMapper menuMapper;

    public MenuServiceImpl(MenuRepository menuRepository, MenuMapper menuMapper) {
        this.menuRepository = menuRepository;
        this.menuMapper = menuMapper;
    }

    @Override
    public Mono<MenuDTO> save(MenuDTO menuDTO) {
        log.debug("Request to save Menu : {}", menuDTO);
        return menuRepository.save(menuMapper.toEntity(menuDTO)).map(menuMapper::toDto);
    }

    @Override
    public Mono<MenuDTO> update(MenuDTO menuDTO) {
        log.debug("Request to save Menu : {}", menuDTO);
        return menuRepository.save(menuMapper.toEntity(menuDTO)).map(menuMapper::toDto);
    }

    @Override
    public Mono<MenuDTO> partialUpdate(MenuDTO menuDTO) {
        log.debug("Request to partially update Menu : {}", menuDTO);

        return menuRepository
            .findById(menuDTO.getId())
            .map(existingMenu -> {
                menuMapper.partialUpdate(existingMenu, menuDTO);

                return existingMenu;
            })
            .flatMap(menuRepository::save)
            .map(menuMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<MenuDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Menus");
        return menuRepository.findAllBy(pageable).map(menuMapper::toDto);
    }

    public Mono<Long> countAll() {
        return menuRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<MenuDTO> findOne(Long id) {
        log.debug("Request to get Menu : {}", id);
        return menuRepository.findById(id).map(menuMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Menu : {}", id);
        return menuRepository.deleteById(id);
    }
}
