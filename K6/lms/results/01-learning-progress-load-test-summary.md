# Load Test Report: 01-learning-progress-load-test

     ✓ progress update: status is 200
     ✓ progress update: response has success envelope

     █ setup

       ✓ login setup: status is 200
       ✓ login setup: has accessToken cookie

     checks...........................: 100.00% ✓ 3994      ✗ 0   
     data_received....................: 4.1 MB  40 kB/s
     data_sent........................: 838 kB  8.2 kB/s
     http_req_blocked.................: avg=20.43µs  min=0s     med=0s      max=1.65ms   p(90)=0s       p(95)=0s       p(99)=952.09µs
     http_req_connecting..............: avg=18.49µs  min=0s     med=0s      max=1.65ms   p(90)=0s       p(95)=0s       p(99)=896.47µs
     http_req_duration................: avg=18.6ms   min=9.43ms med=17.7ms  max=299.48ms p(90)=22.87ms  p(95)=25.18ms  p(99)=35ms    
       { expected_response:true }.....: avg=18.6ms   min=9.43ms med=17.7ms  max=299.48ms p(90)=22.87ms  p(95)=25.18ms  p(99)=35ms    
     ✓ { type:lms_progress_update }...: avg=18.46ms  min=9.43ms med=17.7ms  max=134.97ms p(90)=22.86ms  p(95)=25.17ms  p(99)=34.54ms 
   ✓ http_req_failed..................: 0.00%   ✓ 0         ✗ 1997
     http_req_receiving...............: avg=342.55µs min=0s     med=522.7µs max=3.24ms   p(90)=580.26µs p(95)=790.37µs p(99)=1.09ms  
     http_req_sending.................: avg=4.27µs   min=0s     med=0s      max=764.2µs  p(90)=0s       p(95)=0s       p(99)=0s      
     http_req_tls_handshaking.........: avg=0s       min=0s     med=0s      max=0s       p(90)=0s       p(95)=0s       p(99)=0s      
     http_req_waiting.................: avg=18.25ms  min=9.43ms med=17.34ms max=297.07ms p(90)=22.42ms  p(95)=24.63ms  p(99)=33.94ms 
     http_reqs........................: 1997    19.647567/s
     iteration_duration...............: avg=2.03s    min=1.01s  med=2.03s   max=3.04s    p(90)=2.82s    p(95)=2.92s    p(99)=3s      
     iterations.......................: 1996    19.637729/s
     vus..............................: 3       min=1       max=50
     vus_max..........................: 50      min=50      max=50