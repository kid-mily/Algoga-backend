# Load Test Report: 01-learning-progress-load-test

     ✓ progress update: status is 200
     ✓ progress update: response has success envelope

     █ setup

       ✓ login setup: status is 200
       ✓ login setup: has accessToken cookie

     checks...........................: 100.00% ✓ 62436      ✗ 0    
     data_received....................: 64 MB   622 kB/s
     data_sent........................: 13 MB   128 kB/s
     http_req_blocked.................: avg=38.42µs  min=0s      med=0s      max=22.41ms  p(90)=0s       p(95)=0s       p(99)=1.08ms  
     http_req_connecting..............: avg=24.51µs  min=0s      med=0s      max=12.2ms   p(90)=0s       p(95)=0s       p(99)=1.06ms  
     http_req_duration................: avg=77.99ms  min=11.76ms med=55.39ms max=613.67ms p(90)=165.98ms p(95)=199.48ms p(99)=273.08ms
       { expected_response:true }.....: avg=77.99ms  min=11.76ms med=55.39ms max=613.67ms p(90)=165.98ms p(95)=199.48ms p(99)=273.08ms
     ✓ { type:lms_progress_update }...: avg=77.99ms  min=11.76ms med=55.39ms max=613.67ms p(90)=165.98ms p(95)=199.48ms p(99)=273.09ms
   ✓ http_req_failed..................: 0.00%   ✓ 0          ✗ 31218
     http_req_receiving...............: avg=394.61µs min=0s      med=506.4µs max=47.74ms  p(90)=875.08µs p(95)=1.06ms   p(99)=2.06ms  
     http_req_sending.................: avg=43.11µs  min=0s      med=0s      max=12.98ms  p(90)=0s       p(95)=508.2µs  p(99)=790.4µs 
     http_req_tls_handshaking.........: avg=0s       min=0s      med=0s      max=0s       p(90)=0s       p(95)=0s       p(99)=0s      
     http_req_waiting.................: avg=77.56ms  min=11.76ms med=54.96ms max=612.95ms p(90)=165.6ms  p(95)=199.04ms p(99)=272.87ms
     http_reqs........................: 31218   304.587054/s
     iteration_duration...............: avg=2.08s    min=1.02s   med=2.08s   max=3.55s    p(90)=2.87s    p(95)=2.97s    p(99)=3.09s   
     iterations.......................: 31217   304.577298/s
     vus..............................: 6       min=6        max=800
     vus_max..........................: 800     min=800      max=800