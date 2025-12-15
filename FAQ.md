# FAQ

## Is there any limitation on the number of requests?

No rate limiting is configured in the services or the API gateway. The system will accept as many requests as the underlying infrastructure can handle. If you need to enforce a cap, add a rate limiter at the gateway level (for example, Spring Cloud Gateway's request rate limiter filter) or at your load balancer.
