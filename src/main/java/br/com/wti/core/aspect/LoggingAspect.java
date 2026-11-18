package br.com.wti.core.aspect;

import br.com.wti.core.exception.AcessoNegadoException;
import br.com.wti.core.exception.BusinessException;
import br.com.wti.core.exception.EntidadeNaoEncontradaException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // Set sincronizado com chaves fracas para evitar leak (Throwables podem ser GC'd)
    private static final Set<Throwable> LOGGED_EXCEPTIONS =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    // Exceções esperadas (erros de usuário/validação): não logar com stacktrace;
    // o @ControllerAdvice converte em resposta HTTP amigável.
    private static final Set<Class<? extends Throwable>> EXCECOES_ESPERADAS = Set.of(
            BusinessException.class,
            EntidadeNaoEncontradaException.class,
            AcessoNegadoException.class,
            DataIntegrityViolationException.class);

    @Pointcut("within(@org.springframework.stereotype.Repository *)"
            + " || within(@org.springframework.stereotype.Service *)"
            + " || within(@org.springframework.web.bind.annotation.RestController *)")
    public void springBeanPointcut() {}

    @Pointcut("within(br.com.wti..*)")
    public void applicationPackagePointcut() {}

    @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        if (ehExcecaoEsperada(e)) {
            return;
        }
        // Se já registramos essa instância de Throwable antes, não registramos novamente
        boolean shouldLog = LOGGED_EXCEPTIONS.add(e);
        if (shouldLog) {
            log.error(
                    "Exception {}.{}() com a causa = {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getCause() != null ? e.getCause() : "NULL",
                    e);
        }
    }

    private boolean ehExcecaoEsperada(Throwable e) {
        return EXCECOES_ESPERADAS.stream().anyMatch(cls -> cls.isInstance(e));
    }

    @Around("applicationPackagePointcut() && springBeanPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        UUID randomUUID = UUID.randomUUID();
        StopWatch watch = new StopWatch();

        log.debug(
                "UUID={} Entrada={}.{}() parâmetros={}",
                randomUUID,
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));
        watch.start();
        try {
            return joinPoint.proceed();
        } finally {
            watch.stop();
            log.debug("UUID={}", randomUUID);
            if (watch.getTotalTimeSeconds() >= 3) {
                log.warn(
                        "Tempo de execucao do metodo {}.{} e igual a {}s UUID = {}",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        watch.getTotalTimeSeconds(),
                        randomUUID);
            }
        }
    }
}
