# Load Test Report: 03-my-course-list-load-test

     ✓ my course list: status is 200
     ✓ my course list: response has success envelope

     █ setup

       ✓ login setup: status is 200
       ✓ login setup: has accessToken cookie

     checks..........................: 100.00% ✓ 79952      ✗ 0     
     data_received...................: 130 MB  1.3 MB/s
     data_sent.......................: 14 MB   132 kB/s
     http_req_blocked................: avg=22.83µs  min=0s     med=0s      max=11.85ms  p(90)=0s       p(95)=0s       p(99)=1.04ms  
     http_req_connecting.............: avg=18.8µs   min=0s     med=0s      max=4.23ms   p(90)=0s       p(95)=0s       p(99)=774.62µs
     http_req_duration...............: avg=23.08ms  min=8.87ms med=18.13ms max=715.43ms p(90)=24.51ms  p(95)=31.06ms  p(99)=229.09ms
       { expected_response:true }....: avg=23.08ms  min=8.87ms med=18.13ms max=715.43ms p(90)=24.51ms  p(95)=31.06ms  p(99)=229.09ms
     ✓ { type:lms_my_course_list }...: avg=23.08ms  min=8.87ms med=18.13ms max=715.43ms p(90)=24.51ms  p(95)=31.05ms  p(99)=229.11ms
   ✓ http_req_failed.................: 0.00%   ✓ 0          ✗ 39976 
     http_req_receiving..............: avg=319.92µs min=0s     med=506.4µs max=10.51ms  p(90)=606.04µs p(95)=750.22µs p(99)=1.49ms  
     http_req_sending................: avg=10.53µs  min=0s     med=0s      max=4ms      p(90)=0s       p(95)=0s       p(99)=529.92µs
     http_req_tls_handshaking........: avg=0s       min=0s     med=0s      max=0s       p(90)=0s       p(95)=0s       p(99)=0s      
     http_req_waiting................: avg=22.75ms  min=8.76ms med=17.81ms max=714.93ms p(90)=24.11ms  p(95)=30.61ms  p(99)=228.5ms 
     http_reqs.......................: 39976   389.472852/s
     iteration_duration..............: avg=2.02s    min=1.01s  med=2.03s   max=3.41s    p(90)=2.82s    p(95)=2.92s    p(99)=3s      
     iterations......................: 39975   389.463109/s
     vus.............................: 5       min=5        max=1000
     vus_max.........................: 1000    min=1000     max=1000