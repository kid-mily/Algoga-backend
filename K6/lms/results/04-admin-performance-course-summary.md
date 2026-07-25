# Load Test Report: 04-admin-performance-course

     ✓ admin course list: status is 200
     ✓ admin course list: response has success envelope

     checks...............................: 100.00% ✓ 165538     ✗ 0     
     data_received........................: 894 MB  4.2 MB/s
     data_sent............................: 33 MB   154 kB/s
     http_req_blocked.....................: avg=11.18µs  min=0s     med=0s      max=143.94ms p(90)=0s       p(95)=0s     p(99)=512.66µs
     http_req_connecting..................: avg=8.14µs   min=0s     med=0s      max=10.02ms  p(90)=0s       p(95)=0s     p(99)=505.9µs 
     http_req_duration....................: avg=7.15ms   min=1.51ms med=6.18ms  max=526.75ms p(90)=8.37ms   p(95)=9.47ms p(99)=18.25ms 
       { expected_response:true }.........: avg=7.15ms   min=1.51ms med=6.18ms  max=526.75ms p(90)=8.37ms   p(95)=9.47ms p(99)=18.25ms 
     ✓ { type:admin_coupon_statistics }...: avg=0s       min=0s     med=0s      max=0s       p(90)=0s       p(95)=0s     p(99)=0s      
     ✓ { type:admin_course_list }.........: avg=7.15ms   min=1.51ms med=6.18ms  max=526.75ms p(90)=8.37ms   p(95)=9.47ms p(99)=18.25ms 
   ✓ http_req_failed......................: 0.00%   ✓ 0          ✗ 82769 
     http_req_receiving...................: avg=368.61µs min=0s     med=360.4µs max=86.46ms  p(90)=929.01µs p(95)=1.01ms p(99)=1.52ms  
     http_req_sending.....................: avg=3.49µs   min=0s     med=0s      max=9.01ms   p(90)=0s       p(95)=0s     p(99)=0s      
     http_req_tls_handshaking.............: avg=0s       min=0s     med=0s      max=0s       p(90)=0s       p(95)=0s     p(99)=0s      
     http_req_waiting.....................: avg=6.78ms   min=1.51ms med=5.83ms  max=526.75ms p(90)=7.96ms   p(95)=9ms    p(99)=17.71ms 
     http_reqs............................: 82769   389.274994/s
     iteration_duration...................: avg=2s       min=1s     med=2s      max=3.39s    p(90)=2.81s    p(95)=2.9s   p(99)=2.98s   
     iterations...........................: 82769   389.274994/s
     vus..................................: 3       min=3        max=1000
     vus_max..............................: 1000    min=1000     max=1000