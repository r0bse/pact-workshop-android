package au.com.dius.pactconsumer.data;

import androidx.annotation.NonNull;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import org.joda.time.DateTime;

import java.io.UnsupportedEncodingException;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import au.com.dius.pactconsumer.data.exceptions.BadRequestException;
import au.com.dius.pactconsumer.data.model.ServiceResponse;
import au.com.dius.pactconsumer.util.DateHelper;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

@Singleton
public class Service implements Repository {

  private static final int BAD_REQUEST = 400;
  private static final int NOT_FOUND = 404;

  public interface Api {
    @GET("provider.json")
    Single<ServiceResponse> loadProviderJson(@Query("valid_date") String validDate);
  }

  private final Api api;

  @Inject
  public Service(@NonNull Api api) {
    this.api = api;
  }

  @NonNull
  @Override
  public Single<ServiceResponse> fetchResponse(@NonNull DateTime dateTime) {
    try {
      return api.loadProviderJson(DateHelper.encodeDate(dateTime))
          .onErrorResumeNext(this::mapError);
    } catch (UnsupportedEncodingException e) {
      return Single.error(e);
    }
  }

  private Single<ServiceResponse> mapError(Throwable throwable) {
    if (!(throwable instanceof HttpException)) {
      return Single.error(throwable);
    }

    HttpException exception = (HttpException) throwable;
    if (exception.code() == NOT_FOUND) {
      return Single.just(new ServiceResponse(null, Collections.emptyList()));
    } else if (exception.code() == BAD_REQUEST) {
      return Single.error(new BadRequestException(exception.message(), exception));
    }
    return Single.error(throwable);
  }
}
