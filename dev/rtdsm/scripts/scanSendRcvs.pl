#!/usr/bin/perl
use strict;
use Cwd;
use Math::Complex;
use Switch;

if ($#ARGV != 0) {
    print "usage: <sinkOut>\n";
    exit 2;
}

my $sinkFile = shift;

my $myline;
my $i;


open(FILE,$sinkFile)
   || die "cannot open file $trFile";

while(<FILE>) {

        $myline = $_;
        handleLine($myline);
}

sub handleLine {
  my ($line);
  my @array;
  ($line) = @_;
  $line =~ s/^\s+//;

  if( $line =~ m/Received/i) {
    @array = split(/|/,$line);
		$sink=0 + $array[1];
		$channel=0 + $array[2];
		$time=0 + $array[3];
		$op = $array[4];
		$type = $array[5];
		$dest = $array[3];
		@array = split(/\./,$dest);
		$dest = $array[3];
		@array = split(/\]/,$dest);
		$dest = $array[0];
		$dest = 0 + hex($dest);
		#print("node:$node time:$time ");
		#print("op=$op type=$type dest=$dest\n");
		switch ($type) {
			case "RTS" {handleRTS($node,$op,$dest,$time)}
			case "CTS" {handleCTS($node,$op)}
			case "Data" {handleData($node,$op,$dest,$time)}
			case "Ack" {handleAck($node,$op)}
		}
        }
	elsif ($line =~ m/Retransmit wait for CTS/i) {
		@array = split(/ /,$line);
		$node = 0 + $array[1];
		handleDroppedRTS($node);
	}
	elsif ($line =~ m/Retransmit wait for ACK/i) {
		@array = split(/ /,$line);
		$node = 0 + $array[1];
		handleDroppedData($node);
	}
}

sub handleDroppedData{
	my $dest;
	my $src;
	($src) = @_;
	$src = 0 + $src;
	$dest = $destForSrc[$src];
	if($dest >= 0 ){
		$dataDroppedTime[$dest][$dataDroppedCnt[$dest]] = $txArrStart[$dest][$dataCnt[$dest]];
		$dataDroppedCnt[$dest]++;
	}
}

sub handleDroppedRTS{
	my $dest;
	my $src;
	($src) = @_;
	$src = 0 + $src;
	$dest = $destForSrc[$src];
	if($dest >= 0 ){
		$rtsDroppedTime[$dest][$rtsDroppedCnt[$dest]] = $rtsArrStart[$dest][$rtsCnt[$dest]];
		$rtsDroppedCnt[$dest]++;
	}
}

sub handleData {
	my $node;
	my $op;
	my $dest;
	my $time;
	($node,$op,$dest,$time) = @_;
	switch($op) {
		case "R" {
			$txArrEnd[$dest][$dataCnt[$dest]] = $time;
			$dataCnt[$dest]++;
		}
		case "S" {
			$txArrStart[$dest][$dataCnt[$dest]] = $time;
		}
			
	}
}

sub handleCTS {
}

sub handleRTS {
	my $node;
        my $op;
        my $dest;
        my $time;
        ($node,$op,$dest,$time) = @_;
        switch($op) {
                case "R" {
                        $rtsArrEnd[$dest][$rtsCnt[$dest]] = $time;
                        $rtsCnt[$dest]++;
                }
                case "S" {
			if( $destForSrc[$node] == -1){
				$destForSrc[$node] = $dest;
			}
                        $rtsArrStart[$dest][$rtsCnt[$dest]] = $time;
                }

        }
}

sub handleAck {
}

sub printStats {
	my $i;
	my $j;
	unlink("RTSPoints");
	unlink("DataPoints");
	unlink("RTSDropPoints");
	unlink("DataDropPoints");
	open(RTS,">RTSPoints")
	   || die "cannot open file RTSPoints";
	open(DATA,">DataPoints")
	   || die "cannot open file DataPoints";
	open(RTSDROP,">RTSDropPoints")
	   || die "cannot open file RTSDropPoints";
	open(DATADROP,">DataDropPoints")
	   || die "cannot open file DataDropPoints";
	for( $i = 1; $i <= $numNodes; $i++ ) {
		for($j = 0 ; $j < $rtsCnt[$i]; $j++) {
			print(RTS "$rtsArrStart[$i][$j] $i $rtsArrEnd[$i][$j] $i\n");
		}
		for($j = 0 ; $j < $dataCnt[$i]; $j++) {
			print(DATA "$txArrStart[$i][$j] $i $txArrEnd[$i][$j] $i\n");
		}
		for($j = 0 ; $j < $rtsDroppedCnt[$i]; $j++) {
			print(RTSDROP "$rtsDroppedTime[$i][$j] $i $rtsDroppedTime[$i][$j] $i\n");
		}
		for($j = 0 ; $j < $dataDroppedCnt[$i]; $j++) {
			print(DATADROP "$dataDroppedTime[$i][$j] $i $dataDroppedTime[$i][$j] $i\n");
		}
	}
	close(RTS);
	close(DATA);
	close(RTSDROP);
	close(DATADROP);
}
