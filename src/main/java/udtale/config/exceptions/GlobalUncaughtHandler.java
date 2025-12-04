package udtale.config.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalUncaughtHandler implements Thread.UncaughtExceptionHandler{

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        log.error("Uncaught exception in thread {}: {}", thread.getName(), throwable.toString(), throwable);

    }
}
