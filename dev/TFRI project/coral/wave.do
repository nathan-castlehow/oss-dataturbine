onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/clk
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/reset
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/InstructCount
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/c_cycle
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/c_cycle_next
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/PC
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/PC_next
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/PC_plusone
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/PC_target
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/PC_set
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/IC
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/IC_next
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/instruction_valid
add wave -noupdate -format Literal /test_core/UUT/backend_mk34/instruction_data
add wave -noupdate -color Orange -format Logic /test_core/UUT/backend_mk34/c_is_brnch
add wave -noupdate -color Orange -format Logic /test_core/UUT/backend_mk34/c_compare
add wave -noupdate -divider Backend
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/in_req
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/in_ack
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/in_addr
add wave -noupdate -format Literal /test_core/UUT/backend_mk34/in_data
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/out_req
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/out_ack
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/out_addr
add wave -noupdate -format Literal /test_core/UUT/backend_mk34/out_data
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/op0
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/op1
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/read_sel0
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/read_sel1
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/write_sel
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/reg_din
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/mem_out
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/mem_din
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/c_alu_opcode
add wave -noupdate -format Literal /test_core/UUT/backend_mk34/c_reg_opcode
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_is_set
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_is_from_mem
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_is_datain
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_is_swch
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_is_RT
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_is_call
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_is_dst_op0
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_is_a0
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_is_2ndop
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_is_SP
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_is_a0fpsp
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_is_imd
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_read_write_req
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_write_en
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_stall
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_is_str_dout0
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_reg_write_en
add wave -noupdate -format Logic /test_core/UUT/backend_mk34/c_refused
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/dst_sel
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/alu_out
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/data_wire
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/set_wire
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/data_comb_wire
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/data_full_wire
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/imd_val
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/sp_val
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/fp_val
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/spfp_val
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/spfpa0_val
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/a0_val
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/dout0
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/dout1
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/alu_src0
add wave -noupdate -format Literal -radix decimal /test_core/UUT/backend_mk34/alu_src1
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/backend_mk34/branch_dst
add wave -noupdate -format Literal -radix decimal -expand /test_core/UUT/backend_mk34/reg_bank/regBnk
add wave -noupdate -divider {Fetch Unit}
add wave -noupdate -format Logic -radix binary /test_core/UUT/fetch_mk34/dequeue
add wave -noupdate -format Logic -radix binary /test_core/UUT/fetch_mk34/restart
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/fetch_mk34/restart_addr
add wave -noupdate -format Logic -radix binary /test_core/UUT/fetch_mk34/instruction_valid
add wave -noupdate -format Literal -radix hexadecimal /test_core/UUT/fetch_mk34/instruction_data
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/fetch_mk34/instruction_addr
add wave -noupdate -format Logic -radix binary /test_core/UUT/fetch_mk34/fifo_enqueue
add wave -noupdate -format Logic -radix binary /test_core/UUT/fetch_mk34/fifo_clear
add wave -noupdate -format Logic -radix binary /test_core/UUT/fetch_mk34/fifo_valid
add wave -noupdate -format Logic -radix binary /test_core/UUT/fetch_mk34/fifo_full
add wave -noupdate -format Logic -radix binary /test_core/UUT/fetch_mk34/fifo_empty
add wave -noupdate -format Literal -radix hexadecimal /test_core/UUT/fetch_mk34/ram_data
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/fetch_mk34/pc
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/fetch_mk34/pc_add_one
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/fetch_mk34/pc_next
add wave -noupdate -format Literal -radix unsigned /test_core/UUT/fetch_mk34/pc_prev_r
add wave -noupdate -format Literal /test_core/UUT/fetch_mk34/sel_mux
add wave -noupdate -format Literal /test_core/UUT/fetch_mk34/sel_mux_r
add wave -noupdate -format Logic /test_core/UUT/fetch_mk34/fifo_enque_r
add wave -noupdate -format Logic /test_core/UUT/fetch_mk34/fifo_enque_s1
add wave -noupdate -format Logic /test_core/UUT/fetch_mk34/fifo_clear_r
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 11} {0 ps} 0}
configure wave -namecolwidth 156
configure wave -valuecolwidth 262
configure wave -justifyvalue left
configure wave -signalnamewidth 1
configure wave -snapdistance 10
configure wave -datasetprefix 0
configure wave -rowmargin 4
configure wave -childrowmargin 2
configure wave -gridoffset 0
configure wave -gridperiod 1
configure wave -griddelta 40
configure wave -timeline 0
configure wave -timelineunits ns
update
WaveRestoreZoom {10980392 ps} {11243392 ps}
