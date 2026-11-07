package com.ucop.desktop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan("com.ucop.desktop")
@EnableJpaRepositories("com.ucop.desktop.repo")
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class AppConfig {
    @Bean
    static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    DataSource dataSource(@Value("${db.url}") String url,
                          @Value("${db.auth}") String auth,
                          @Value("${db.username}") String username,
                          @Value("${db.password}") String password) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        ds.setUrl(url);
        if (!"windows".equalsIgnoreCase(auth)) {
            ds.setUsername(username);
            ds.setPassword(password);
        }
        return ds;
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, @Value("${db.ddl}") String ddl) {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.ucop.desktop.domain");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Properties props = new Properties();
        props.put("hibernate.hbm2ddl.auto", ddl);
        props.put("hibernate.dialect", "org.hibernate.dialect.SQLServer2012Dialect");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "true");
        factory.setJpaProperties(props);
        return factory;
    }

    @Bean
    JpaTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
