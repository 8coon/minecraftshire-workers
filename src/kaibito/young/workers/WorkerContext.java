package kaibito.young.workers;


public class WorkerContext<T> {

    private T payload;
    private WorkerDoneCallback<T> done;
    private WorkerFailCallback<T> fail;


    public WorkerContext(T payload, WorkerDoneCallback<T> done, WorkerFailCallback<T> fail) {
        this.payload = payload;
        this.done = done;
        this.fail = fail;
    }


    public T getPayload() {
        return payload;
    }

    public WorkerDoneCallback<T> getDone() {
        return done;
    }

    public WorkerFailCallback<T> getFail() {
        return fail;
    }

}
