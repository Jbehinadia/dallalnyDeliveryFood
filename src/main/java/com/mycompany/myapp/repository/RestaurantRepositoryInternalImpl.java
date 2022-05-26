package com.mycompany.myapp.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import com.mycompany.myapp.domain.Restaurant;
import com.mycompany.myapp.repository.rowmapper.ResponsableRestaurantRowMapper;
import com.mycompany.myapp.repository.rowmapper.RestaurantRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive custom repository implementation for the Restaurant entity.
 */
@SuppressWarnings("unused")
class RestaurantRepositoryInternalImpl extends SimpleR2dbcRepository<Restaurant, Long> implements RestaurantRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final ResponsableRestaurantRowMapper responsablerestaurantMapper;
    private final RestaurantRowMapper restaurantMapper;

    private static final Table entityTable = Table.aliased("restaurant", EntityManager.ENTITY_ALIAS);
    private static final Table ResponsableRestaurantTable = Table.aliased("ResponsableRestaurant", "ResponsableRestaurant");

    public RestaurantRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        ResponsableRestaurantRowMapper responsablerestaurantMapper,
        RestaurantRowMapper restaurantMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Restaurant.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.responsablerestaurantMapper = responsablerestaurantMapper;
        this.restaurantMapper = restaurantMapper;
    }

    @Override
    public Flux<Restaurant> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Restaurant> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = RestaurantSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(ResponsableRestaurantSqlHelper.getColumns(ResponsableRestaurantTable, "ResponsableRestaurant"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(ResponsableRestaurantTable)
            .on(Column.create("responsable_restaurant_id", entityTable))
            .equals(Column.create("id", ResponsableRestaurantTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Restaurant.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Restaurant> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Restaurant> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<Restaurant> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<Restaurant> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Restaurant> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Restaurant process(Row row, RowMetadata metadata) {
        Restaurant entity = restaurantMapper.apply(row, "e");
        entity.setResponsableRestaurant(responsablerestaurantMapper.apply(row, "ResponsableRestaurant"));
        return entity;
    }

    @Override
    public <S extends Restaurant> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
