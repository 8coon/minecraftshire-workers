package kaibito.young.workers;


public interface WorkerDoneCallback<T> {
    void call(Worker<T> worker, T payload);
}
