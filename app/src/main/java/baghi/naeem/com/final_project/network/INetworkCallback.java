package baghi.naeem.com.final_project.network;

public interface INetworkCallback<T> {

    public void callback(T result, String errorMessage);

}
