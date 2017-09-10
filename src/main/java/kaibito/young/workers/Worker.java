package kaibito.young.workers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public abstract class Worker<T> implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int QUEUE_ITEMS_DELAY = 20;
    public static final int QUEUE_DEBOUNCE_THRESHOLD = 10;


    protected abstract void process(T payload) throws Throwable;


    private final WorkerDoneCallback<T> defaultDone = (Worker<T> worker, T payload) -> {};
    private final WorkerFailCallback<T> defaultFail = (Worker<T> worker, Throwable error) -> {
        logger.error("Worker job failed!", error);
    };

    private Queue<WorkerContext<T>> queue = new ConcurrentLinkedQueue<>();
    private AtomicBoolean running = new AtomicBoolean(true);


    @Override
    public void run() {
        if (!this.getClass().isAnnotationPresent(ScheduledWorker.class)) {
            logger.warn("Worker " + this.getClass().getName() + " does not have a " +
                    "ScheduledWorker annotation - cannot set worker delay! Aborting.");
            return;
        }

        int delay = this.getClass().getAnnotation(ScheduledWorker.class).value();

        while (this.isRunning()) {
            try {
                this.runDelayed(delay);
            } catch (InterruptedException e) {
                logger.warn("Error during worker job", e);
            }
        }
    }


    private void runDelayed(int delay) throws InterruptedException {
        for (WorkerContext<T> context = queue.poll(); context != null; context = queue.poll()) {
            try {
                this.process(context.getPayload());
            } catch (Throwable throwable) {
                context.getFail().call(this, throwable);
            }

            context.getDone().call(this, context.getPayload());

            // Спим, если в очереди мало запросов
            if (queue.size() < Worker.QUEUE_DEBOUNCE_THRESHOLD) {
                Thread.sleep(Worker.QUEUE_ITEMS_DELAY);
            }
        }

        Thread.sleep(delay);
    }


    public void schedule(T payload, WorkerDoneCallback<T> done, WorkerFailCallback<T> fail) {
        queue.offer(new WorkerContext<T>(payload, done, fail));
    }

    public void schedule(T payload, WorkerDoneCallback<T> done) {
        this.schedule(payload, done, this.defaultFail);
    }

    public void schedule(T payload, WorkerFailCallback<T> fail) {
        this.schedule(payload, this.defaultDone, fail);
    }

    public void schedule(T payload) {
        this.schedule(payload, this.defaultDone, this.defaultFail);
    }


    public boolean isRunning() {
        return this.running.get();
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }

}
