from junitparser import JUnitXml
from junitparser import TestSuite, TestCase
import argparse


def parse(in_file):
    suite: TestSuite = JUnitXml.fromfile(in_file)
    total = suite.tests - suite.skipped
    error = suite.errors + suite.failures
    success_rate = (total - error) / total * 100.0
    emoji = '😀' if success_rate > 95 else '🫣' if success_rate > 50 else '😟' if success_rate > 25 else '😔'
    print(f"Espresso Test Run {emoji} Success {int(success_rate)}% - Total {total}")
    print()

    for case in suite:
        case: TestCase = case
        print(f"Test: {'✅' if case.is_passed else '❌'} {case.name}")


# init

parser = argparse.ArgumentParser(description='Parses junit xml report files and reports stats')

parser.add_argument('report',
                    type=argparse.FileType('r', encoding='UTF-8'),
                    help='input file')

args = parser.parse_args()

parse(
    in_file=args.report
)
