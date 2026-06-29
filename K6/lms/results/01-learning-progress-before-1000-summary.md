# Load Test Report: 01-learning-progress-load-test

     ✓ progress update: status is 200
     ✓ progress update: response has success envelope

     █ setup

       ✓ login setup: status is 200
       ✓ login setup: has accessToken cookie

     checks...........................: 100.00% ✓ 69230      ✗ 0     
     data_received....................: 71 MB   691 kB/s
     data_sent........................: 15 MB   142 kB/s
     http_req_blocked.................: avg=43.06µs  min=0s      med=0s       max=14.3ms   p(90)=0s       p(95)=0s       p(99)=1.11ms  
     http_req_connecting..............: avg=30.98µs  min=0s      med=0s       max=13.79ms  p(90)=0s       p(95)=0s       p(99)=1.09ms  
     http_req_duration................: avg=343.2ms  min=14.27ms med=393.84ms max=1.63s    p(90)=549.24ms p(95)=642.74ms p(99)=795.82ms
       { expected_response:true }.....: avg=343.2ms  min=14.27ms med=393.84ms max=1.63s    p(90)=549.24ms p(95)=642.74ms p(99)=795.82ms
     ✓ { type:lms_progress_update }...: avg=343.21ms min=14.27ms med=393.84ms max=1.63s    p(90)=549.24ms p(95)=642.75ms p(99)=795.82ms
   ✓ http_req_failed..................: 0.00%   ✓ 0          ✗ 34615 
     http_req_receiving...............: avg=414.39µs min=0s      med=512.2µs  max=104.92ms p(90)=772.46µs p(95)=1.04ms   p(99)=2.07ms  
     http_req_sending.................: avg=40.76µs  min=0s      med=0s       max=35.06ms  p(90)=0s       p(95)=507.8µs  p(99)=719.9µs 
     http_req_tls_handshaking.........: avg=0s       min=0s      med=0s       max=0s       p(90)=0s       p(95)=0s       p(99)=0s      
     http_req_waiting.................: avg=342.75ms min=14.27ms med=393.42ms max=1.63s    p(90)=548.76ms p(95)=642.4ms  p(99)=795.22ms
     http_reqs........................: 34615   338.30205/s
     iteration_duration...............: avg=2.34s    min=1.02s   med=2.34s    max=3.99s    p(90)=3.17s    p(95)=3.31s    p(99)=3.48s   
     iterations.......................: 34614   338.292277/s
     vus..............................: 4       min=4        max=1000
     vus_max..........................: 1000    min=1000     max=1000