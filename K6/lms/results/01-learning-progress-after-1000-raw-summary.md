# Load Test Report: 01-learning-progress-load-test

     ✓ progress update: status is 200
     ✓ progress update: response has success envelope

     █ setup

       ✓ login setup: status is 200
       ✓ login setup: has accessToken cookie

     checks...........................: 100.00% ✓ 80068      ✗ 0     
     data_received....................: 82 MB   797 kB/s
     data_sent........................: 17 MB   164 kB/s
     http_req_blocked.................: avg=25.88µs min=0s      med=0s      max=5.03ms   p(90)=0s      p(95)=0s       p(99)=1.06ms  
     http_req_connecting..............: avg=20.75µs min=0s      med=0s      max=4.85ms   p(90)=0s      p(95)=0s       p(99)=1.04ms  
     http_req_duration................: avg=27.39ms min=10.31ms med=21.12ms max=284.11ms p(90)=45.84ms p(95)=60.87ms  p(99)=104.55ms
       { expected_response:true }.....: avg=27.39ms min=10.31ms med=21.12ms max=284.11ms p(90)=45.84ms p(95)=60.87ms  p(99)=104.55ms
     ✓ { type:lms_progress_update }...: avg=27.39ms min=10.31ms med=21.12ms max=284.11ms p(90)=45.84ms p(95)=60.86ms  p(99)=104.56ms
   ✓ http_req_failed..................: 0.00%   ✓ 0          ✗ 40034 
     http_req_receiving...............: avg=325.2µs min=0s      med=504.9µs max=10.07ms  p(90)=631.5µs p(95)=852.62µs p(99)=1.51ms  
     http_req_sending.................: avg=18.59µs min=0s      med=0s      max=4.54ms   p(90)=0s      p(95)=0s       p(99)=565.2µs 
     http_req_tls_handshaking.........: avg=0s      min=0s      med=0s      max=0s       p(90)=0s      p(95)=0s       p(99)=0s      
     http_req_waiting.................: avg=27.05ms min=9.94ms  med=20.77ms max=282.59ms p(90)=45.38ms p(95)=60.48ms  p(99)=104.11ms
     http_reqs........................: 40034   390.249748/s
     iteration_duration...............: avg=2.02s   min=1.01s   med=2.02s   max=3.18s    p(90)=2.82s   p(95)=2.92s    p(99)=3s      
     iterations.......................: 40033   390.24/s
     vus..............................: 2       min=2        max=1000
     vus_max..........................: 1000    min=1000     max=1000