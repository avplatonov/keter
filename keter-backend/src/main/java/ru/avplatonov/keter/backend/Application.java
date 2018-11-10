package ru.avplatonov.keter.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import ru.avplatonov.keter.backend.db.GraphsDB;
import ru.avplatonov.keter.backend.db.NodesDB;

import java.util.Arrays;

@SpringBootApplication
public class Application {

    public static ApplicationContext context;
    public static GraphsDB graphsDB;
    public static NodesDB nodesDB;

	public static void main(String[] args) {
        context = new AnnotationConfigApplicationContext(GraphsDB.class, NodesDB.class);
        graphsDB = context.getBean(GraphsDB.class);
        nodesDB = context.getBean(NodesDB.class);
        SpringApplication.run(Application.class, args);
    }

    @Configuration
    static class WebSecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers("/**");
        }
    }

    @Bean
    public InternalResourceViewResolver jspViewResolver() {
        InternalResourceViewResolver resolver= new InternalResourceViewResolver();
        resolver.setPrefix("/");
        return resolver;
    }

    @Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			System.out.println("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}
		};
	}
}
