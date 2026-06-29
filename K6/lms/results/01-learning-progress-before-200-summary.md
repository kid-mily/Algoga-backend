# Load Test Report: 01-learning-progress-load-test

     ✓ progress update: status is 200
     ✓ progress update: response has success envelope

     █ setup

       ✓ login setup: status is 200
       ✓ login setup: has accessToken cookie

     checks...........................: 100.00% ✓ 16078     ✗ 0    
     data_received....................: 16 MB   162 kB/s
     data_sent........................: 3.4 MB  33 kB/s
     http_req_blocked.................: avg=17.67µs  min=0s     med=0s      max=1.24ms   p(90)=0s       p(95)=0s       p(99)=605.27µs
     http_req_connecting..............: avg=15.8µs   min=0s     med=0s      max=1.24ms   p(90)=0s       p(95)=0s       p(99)=561.45µs
     http_req_duration................: avg=24.13ms  min=12.3ms med=23.42ms max=119.25ms p(90)=29.44ms  p(95)=31.99ms  p(99)=40.47ms 
       { expected_response:true }.....: avg=24.13ms  min=12.3ms med=23.42ms max=119.25ms p(90)=29.44ms  p(95)=31.99ms  p(99)=40.47ms 
     ✓ { type:lms_progress_update }...: avg=24.13ms  min=12.3ms med=23.42ms max=119.25ms p(90)=29.44ms  p(95)=31.99ms  p(99)=40.05ms 
   ✓ http_req_failed..................: 0.00%   ✓ 0         ✗ 8039 
     http_req_receiving...............: avg=348.13µs min=0s     med=499.5µs max=5.37ms   p(90)=748.89µs p(95)=926.92µs p(99)=1.3ms   
     http_req_sending.................: avg=4.55µs   min=0s     med=0s      max=1.63ms   p(90)=0s       p(95)=0s       p(99)=0s      
     http_req_tls_handshaking.........: avg=0s       min=0s     med=0s      max=0s       p(90)=0s       p(95)=0s       p(99)=0s      
     http_req_waiting.................: avg=23.78ms  min=12.3ms med=23.07ms max=118.67ms p(90)=29.09ms  p(95)=31.59ms  p(99)=40.07ms 
     http_reqs........................: 8039    79.436198/s
     iteration_duration...............: avg=2.01s    min=1.02s  med=2s      max=3.03s    p(90)=2.82s    p(95)=2.91s    p(99)=3s      
     iterations.......................: 8038    79.426317/s
     vus..............................: 1       min=1       max=200
     vus_max..........................: 200     min=200     max=200