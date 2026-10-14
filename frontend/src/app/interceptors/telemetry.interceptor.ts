import { HttpInterceptorFn } from '@angular/common/http';
import { tap } from 'rxjs/operators';

export const telemetryInterceptor: HttpInterceptorFn = (req, next) => {
  const started = performance.now();
  const cloned = req.clone({
    setHeaders: {
      'X-NexusPay-Client': 'Angular-18-Control-Tower',
      'X-Correlation-ID': crypto.randomUUID()
    }
  });

  return next(cloned).pipe(
    tap(() => {
      const elapsed = (performance.now() - started).toFixed(1);
      console.debug(`[API-GATEWAY-TRACE] ${req.method} ${req.urlWithParams} completed in ${elapsed}ms`);
    })
  );
};
