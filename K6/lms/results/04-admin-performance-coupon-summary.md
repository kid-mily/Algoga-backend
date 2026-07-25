# Load Test Report: 04-admin-performance-coupon

     ✓ coupon statistics: status is 200
     ✓ coupon statistics: response has success envelope

     checks...............................: 100.00% ✓ 165228     ✗ 0     
     data_received........................: 408 MB  1.9 MB/s
     data_sent............................: 33 MB   156 kB/s
     http_req_blocked.....................: avg=9.24µs   min=0s      med=0s      max=3.16ms   p(90)=0s      p(95)=0s      p(99)=520.17µs
     http_req_connecting..................: avg=8.03µs   min=0s      med=0s      max=2.65ms   p(90)=0s      p(95)=0s      p(99)=511.17µs
     http_req_duration....................: avg=9.7ms    min=395.4µs med=9.05ms  max=387.85ms p(90)=11.1ms  p(95)=12.09ms p(99)=18.21ms 
       { expected_response:true }.........: avg=9.7ms    min=395.4µs med=9.05ms  max=387.85ms p(90)=11.1ms  p(95)=12.09ms p(99)=18.21ms 
     ✓ { type:admin_coupon_statistics }...: avg=9.7ms    min=395.4µs med=9.05ms  max=387.85ms p(90)=11.1ms  p(95)=12.09ms p(99)=18.21ms 
     ✓ { type:admin_course_list }.........: avg=0s       min=0s      med=0s      max=0s       p(90)=0s      p(95)=0s      p(99)=0s      
   ✓ http_req_failed......................: 0.00%   ✓ 0          ✗ 82614 
     http_req_receiving...................: avg=420.23µs min=0s      med=355.3µs max=13.35ms  p(90)=1ms     p(95)=1.38ms  p(99)=3.18ms  
     http_req_sending.....................: avg=3.81µs   min=0s      med=0s      max=6.18ms   p(90)=0s      p(95)=0s      p(99)=0s      
     http_req_tls_handshaking.............: avg=0s       min=0s      med=0s      max=0s       p(90)=0s      p(95)=0s      p(99)=0s      
     http_req_waiting.....................: avg=9.27ms   min=382.1µs med=8.69ms  max=387.85ms p(90)=10.72ms p(95)=11.69ms p(99)=17.62ms 
     http_reqs............................: 82614   390.118173/s
     iteration_duration...................: avg=2.01s    min=1s      med=2.01s   max=3.22s    p(90)=2.8s    p(95)=2.91s   p(99)=2.98s   
     iterations...........................: 82614   390.118173/s
     vus..................................: 12      min=12       max=1000
     vus_max..............................: 1000    min=1000     max=1000