import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.Log;

public class CustomLogChute implements LogChute {

    private Log logger;

    @Override
    public void init(RuntimeServices runtimeServices) throws Exception {
        // 可以在这里进行初始化操作，比如获取日志实例
        logger = runtimeServices.getLog();
    }

    @Override
    public void log(int level, String message) {
        // 在这里实现自定义的日志输出逻辑
        // level 参数是日志级别，message 参数是日志消息
        switch (level) {
            case LogChute.DEBUG_ID:
                logger.debug(message);
                break;
            case LogChute.INFO_ID:
                logger.info(message);
                break;
            case LogChute.WARN_ID:
                logger.warn(message);
                break;
            case LogChute.ERROR_ID:
                logger.error(message);
                break;
            default:
                logger.info(message); // 默认使用 INFO 级别输出
        }
    }

    @Override
    public void log(int level, String message, Throwable throwable) {
        // 可以处理带有异常信息的日志消息
        // 在这里实现自定义的日志输出逻辑
        switch (level) {
            case LogChute.DEBUG_ID:
                logger.debug(message, throwable);
                break;
            case LogChute.INFO_ID:
                logger.info(message, throwable);
                break;
            case LogChute.WARN_ID:
                logger.warn(message, throwable);
                break;
            case LogChute.ERROR_ID:
                logger.error(message, throwable);
                break;
            default:
                logger.info(message, throwable); // 默认使用 INFO 级别输出
        }
    }

    @Override
    public boolean isLevelEnabled(int level) {
        // 可以实现检查特定日志级别是否启用的逻辑
        // 在这里可以根据需要自定义
        return true; // 默认始终启用日志
    }
}
