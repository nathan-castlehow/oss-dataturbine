MODE 2
SCAN RATE 30

; Set high-res output.
2:P78
1:1

; Turn on output flag.
3:P86
1:10

; Record the time.
4:P77
1:0021

1:P105
1:1	; Address 1
2:11	; ID command
3:1	; Control port 1
4:1	; Input loc 2
5:1	; Multiplier
6:0	; Offset

1:P105
1:1	; Address 1
2:11	; ID command
3:1	; Control port 1
4:1	; Input loc 2
5:1	; Multiplier
6:0	; Offset

1:P105
1:2	; Address 1
2:11	; ID command
3:1	; Control port 1
4:1	; Input loc 2
5:1	; Multiplier
6:0	; Offset

1:P105
1:3	; Address 1
2:11	; ID command
3:1	; Control port 1
4:1	; Input loc 2
5:1	; Multiplier
6:0	; Offset

; Record the time again.
4:P77
1:0021

; Sample 1 locations, starting from loc 1.
5:P70
1:1
2:1
