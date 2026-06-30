# Load Test Report: 02-public-course-list-load-test

     ✓ public course list: status is 200
     ✓ public course list: response has success envelope

     checks..............................: 100.00% ✓ 63806     ✗ 0    
     data_received.......................: 142 MB  1.4 MB/s
     data_sent...........................: 4.0 MB  39 kB/s
     http_req_blocked....................: avg=31.54µs  min=0s     med=0s      max=18.68ms  p(90)=0s      p(95)=0s       p(99)=1.06ms  
     http_req_connecting.................: avg=24.01µs  min=0s     med=0s      max=9.71ms   p(90)=0s      p(95)=0s       p(99)=1.01ms  
     http_req_duration...................: avg=28.31ms  min=9.37ms med=19.46ms max=881.99ms p(90)=35.21ms p(95)=56.99ms  p(99)=279.32ms
       { expected_response:true }........: avg=28.31ms  min=9.37ms med=19.46ms max=881.99ms p(90)=35.21ms p(95)=56.99ms  p(99)=279.32ms
     ✓ { type:lms_public_course_list }...: avg=28.31ms  min=9.37ms med=19.46ms max=881.99ms p(90)=35.21ms p(95)=56.99ms  p(99)=279.32ms
   ✓ http_req_failed.....................: 0.00%   ✓ 0         ✗ 31903
     http_req_receiving..................: avg=362.23µs min=0s     med=522.8µs max=10.96ms  p(90)=649.1µs p(95)=923.91µs p(99)=1.54ms  
     http_req_sending....................: avg=16.61µs  min=0s     med=0s      max=7.72ms   p(90)=0s      p(95)=0s       p(99)=569.99µs
     http_req_tls_handshaking............: avg=0s       min=0s     med=0s      max=0s       p(90)=0s      p(95)=0s       p(99)=0s      
     http_req_waiting....................: avg=27.93ms  min=8.52ms med=19.11ms max=878.83ms p(90)=34.74ms p(95)=56.44ms  p(99)=278.32ms
     http_reqs...........................: 31903   312.14794/s
     iteration_duration..................: avg=2.03s    min=1.01s  med=2.03s   max=3.69s    p(90)=2.83s   p(95)=2.92s    p(99)=3s      
     iterations..........................: 31903   312.14794/s
     vus.................................: 3       min=3       max=800
     vus_max.............................: 800     min=800     max=800