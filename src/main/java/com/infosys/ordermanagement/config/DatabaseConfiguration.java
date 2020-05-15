package com.infosys.ordermanagement.config;


import com.infosys.ordermanagement.OrdermanagementApplication;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

@Profile("aws")
@Configuration
@EnableJpaRepositories(
        basePackages = "com.infosys.ordermanagement",
        entityManagerFactoryRef = "customEntityManager"
)
public class DatabaseConfiguration {

    private final String PACKAGE_SCAN = "com.infosys.ordermanagement";

    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.h2.Driver");
        dataSourceBuilder.url("jdbc:h2:mem:test");
        dataSourceBuilder.username("SA");
        dataSourceBuilder.password("");
        return dataSourceBuilder.build();
    }

    @Primary
    @Bean(name = "awsDataSource")
    public DataSource awsDataSource() {
        Properties prop = OrdermanagementApplication.PROP;
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(prop.getProperty("spring.datasource.url"))
                .username(prop.getProperty("spring.datasource.username"))
                .password(prop.getProperty("spring.datasource.password"))
                .build();
    }



    @Bean(name = "customEntityManager")
    public LocalContainerEntityManagerFactoryBean customEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(awsDataSource());
        em.setPackagesToScan(PACKAGE_SCAN);
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());
        return em;
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.ddl-auto", "update");
        properties.put("generate-ddl", true);
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);
        return properties;
    }

}
