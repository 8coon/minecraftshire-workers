package kaibito.young.workers;


public interface WorkerFailCallback<T> {
    void call(Worker<T> worker, Throwable error);
}
