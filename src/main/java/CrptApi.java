import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final AtomicInteger requestCount;
    private long lastResetTime;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.requestCount = new AtomicInteger(0);
        this.lastResetTime = System.currentTimeMillis();
    }

    public synchronized void createDocument(Object document, String signature) {
        // проверка на сброс счетчика запросов
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime >= timeUnit.toMillis(1)) {
            requestCount.set(0);
            lastResetTime = currentTime;
        }

        // проверка на превышение ограничения на количество запросов
        if (requestCount.get() >= requestLimit) {
            try {
                wait(); // если превышено, блокируем вызов метода
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Вызов метода API для создания документа: " + document);

        // увеличиваем счетчик запросов
        requestCount.incrementAndGet();
        notifyAll(); // оповещание потоков, которые ждут разблокировки
    }

    public static void main(String[]args){

        CrptApi crptApi=new CrptApi(TimeUnit.SECONDS,3);

        Object document=new Object();
        String signature="example_signature";

        crptApi.createDocument(document,signature);
    }
}
