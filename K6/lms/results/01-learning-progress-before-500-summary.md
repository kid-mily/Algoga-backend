# Load Test Report: 01-learning-progress-load-test

     ✓ progress update: status is 200
     ✓ progress update: response has success envelope

     █ setup

       ✓ login setup: status is 200
       ✓ login setup: has accessToken cookie

     checks...........................: 100.00% ✓ 40066      ✗ 0    
     data_received....................: 41 MB   400 kB/s
     data_sent........................: 8.4 MB  82 kB/s
     http_req_blocked.................: avg=19.51µs  min=0s      med=0s      max=3.13ms  p(90)=0s      p(95)=0s       p(99)=714.08µs
     http_req_connecting..............: avg=17.05µs  min=0s      med=0s      max=2.66ms  p(90)=0s      p(95)=0s       p(99)=581.28µs
     http_req_duration................: avg=24.93ms  min=12.35ms med=24.47ms max=98.35ms p(90)=28.66ms p(95)=30.96ms  p(99)=39.51ms 
       { expected_response:true }.....: avg=24.93ms  min=12.35ms med=24.47ms max=98.35ms p(90)=28.66ms p(95)=30.96ms  p(99)=39.51ms 
     ✓ { type:lms_progress_update }...: avg=24.92ms  min=12.35ms med=24.47ms max=98.35ms p(90)=28.66ms p(95)=30.95ms  p(99)=39.48ms 
   ✓ http_req_failed..................: 0.00%   ✓ 0          ✗ 20033
     http_req_receiving...............: avg=314.68µs min=0s      med=453.6µs max=18.71ms p(90)=603.9µs p(95)=761.79µs p(99)=1.17ms  
     http_req_sending.................: avg=6.79µs   min=0s      med=0s      max=2.01ms  p(90)=0s      p(95)=0s       p(99)=509.6µs 
     http_req_tls_handshaking.........: avg=0s       min=0s      med=0s      max=0s      p(90)=0s      p(95)=0s       p(99)=0s      
     http_req_waiting.................: avg=24.6ms   min=12.12ms med=24.17ms max=85.76ms p(90)=28.32ms p(95)=30.61ms  p(99)=39.11ms 
     http_reqs........................: 20033   195.991822/s
     iteration_duration...............: avg=2.02s    min=1.01s   med=2.02s   max=3.06s   p(90)=2.82s   p(95)=2.92s    p(99)=3s      
     iterations.......................: 20032   195.982038/s
     vus..............................: 1       min=1        max=500
     vus_max..........................: 500     min=500      max=500