package com.jshvarts.conductormvp.editnote

import com.jshvarts.conductormvp.domain.NoteRepository
import com.jshvarts.conductormvp.domain.model.Note
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class EditNotePresenter @Inject constructor(private val repository: NoteRepository) : EditNoteContract.Presenter {

    private lateinit var view: EditNoteView

    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun detachView() {
        Timber.d("EditNotePresenter::detachView")
        disposables.clear()
    }

    override fun attachView(view: EditNoteView) {
        Timber.d("EditNotePresenter::attachView")
        this.view = view
    }

    override fun loadNote(id: Long) {
        disposables.add(repository.findNoteById(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::displayNoteForEdit, this::showUnableToLoadNoteError))
    }

    override fun editNote(id: Long, noteText: String) {
        val note = Note(id, noteText)
        if (!note.isValid()) {
            view.onNoteValidationFailed()
            return
        }
        disposables.add(repository.update(note)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::onNoteEditSuccess, this::showUnableToEditNoteError))
    }

    private fun showUnableToLoadNoteError(error: Throwable) {
        Timber.e(error)
        view.onNoteLookupFailed()
    }

    private fun showUnableToEditNoteError(error: Throwable) {
        Timber.e(error)
        view.onNoteEditFailed()
    }
}