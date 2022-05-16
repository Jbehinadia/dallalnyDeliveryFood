package com.mycompany.myapp.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import com.mycompany.myapp.domain.Commande;
import com.mycompany.myapp.repository.rowmapper.ClientRowMapper;
import com.mycompany.myapp.repository.rowmapper.CommandeRowMapper;
import com.mycompany.myapp.repository.rowmapper.LivreurRowMapper;
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
 * Spring Data SQL reactive custom repository implementation for the Commande entity.
 */
@SuppressWarnings("unused")
class CommandeRepositoryInternalImpl extends SimpleR2dbcRepository<Commande, Long> implements CommandeRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final LivreurRowMapper livreurMapper;
    private final ClientRowMapper clientMapper;
    private final CommandeRowMapper commandeMapper;

    private static final Table entityTable = Table.aliased("commande", EntityManager.ENTITY_ALIAS);
    private static final Table livreurTable = Table.aliased("livreur", "livreur");
    private static final Table clientTable = Table.aliased("client", "client");

    public CommandeRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        LivreurRowMapper livreurMapper,
        ClientRowMapper clientMapper,
        CommandeRowMapper commandeMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Commande.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.livreurMapper = livreurMapper;
        this.clientMapper = clientMapper;
        this.commandeMapper = commandeMapper;
    }

    @Override
    public Flux<Commande> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Commande> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = CommandeSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(LivreurSqlHelper.getColumns(livreurTable, "livreur"));
        columns.addAll(ClientSqlHelper.getColumns(clientTable, "client"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(livreurTable)
            .on(Column.create("livreur_id", entityTable))
            .equals(Column.create("id", livreurTable))
            .leftOuterJoin(clientTable)
            .on(Column.create("client_id", entityTable))
            .equals(Column.create("id", clientTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Commande.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Commande> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Commande> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private Commande process(Row row, RowMetadata metadata) {
        Commande entity = commandeMapper.apply(row, "e");
        entity.setLivreur(livreurMapper.apply(row, "livreur"));
        entity.setClient(clientMapper.apply(row, "client"));
        return entity;
    }

    @Override
    public <S extends Commande> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
