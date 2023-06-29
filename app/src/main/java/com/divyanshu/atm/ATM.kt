package com.divyanshu.atm

class ATM {
    private val denominationCount = mutableMapOf(
        2000 to 0,
        500 to 0,
        200 to 0,
        100 to 0
    )

    fun deposit(denomination: Int, count: Int) {
        if (denominationCount.containsKey(denomination)) {
            val currentCount = denominationCount[denomination] ?: 0
            denominationCount[denomination] = currentCount + count
        }
    }

    fun withdraw(amount: Int): WithdrawalResult {
        val withdrawalResult = WithdrawalResult(false, 0.toString())

        if (amount <= 0) {
            return withdrawalResult
        }

        val sortedDenomination = denominationCount.keys.sortedDescending()

        val remainingAmount = withdrawAmountWithDenomination(sortedDenomination, amount)

        if (remainingAmount == 0) {
            withdrawalResult.isSuccessful = true
            withdrawalResult.denomination = getWithdrawnDenomination(amount)
        }

        return withdrawalResult
    }

    private fun withdrawAmountWithDenomination(
        denominations: List<Int>,
        amount: Int
    ): Int {
        var remainingAmount = amount

        for (denomination in denominations) {
            val count = denominationCount[denomination] ?: 0
            val requiredCount = remainingAmount / denomination
            val actualCount = minOf(count, requiredCount)

            denominationCount[denomination] = count - actualCount
            remainingAmount -= actualCount * denomination

            if (remainingAmount == 0) {
                break
            }
        }

        return remainingAmount
    }

    private fun getWithdrawnDenomination(amount: Int): String {
        val withdrawnDenomination = mutableMapOf<Int, Int>()

        var remainingAmount = amount

        for (denomination in denominationCount.keys.sortedDescending()) {
            val count = minOf(denominationCount[denomination] ?: 0, remainingAmount / denomination)
            withdrawnDenomination[denomination] = count
            remainingAmount -= count * denomination
        }

        val withdrawnDenominationString = StringBuilder()

        for ((denomination, count) in withdrawnDenomination) {
            if (count > 0) {
                withdrawnDenominationString.append("$denomination x $count, ")
            }
        }

        return withdrawnDenominationString.toString().trimEnd(',', ' ')
    }

    fun getAvailableDenomination(): String {
        val availableDenominationString = StringBuilder()

        for ((denomination, count) in denominationCount) {
            if (count > 0) {
                availableDenominationString.append("$denomination x $count, ")
            }
        }

        return availableDenominationString.toString().trimEnd(',', ' ')
    }
}

data class WithdrawalResult(var isSuccessful: Boolean, var denomination: String)