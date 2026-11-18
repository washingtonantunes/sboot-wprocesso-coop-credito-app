package br.com.wti.core.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FuncionalidadeAutorizada {

    /**
     * Códigos de funcionalidade aceitos. Semântica OR: o usuário precisa ter
     * pelo menos um deles para acessar o endpoint.
     */
    String[] value();
}
