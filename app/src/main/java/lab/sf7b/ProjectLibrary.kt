package lab.sf7b

// Pure JVM implementation keeps the persistence contract testable without Android.
typealias ProjectLibrary = lab.sf7b.core.ProjectLibrary
typealias ProjectEntry = lab.sf7b.core.ProjectEntry
typealias OpenProject = lab.sf7b.core.OpenProject
typealias RecoveryNotice = lab.sf7b.core.RecoveryNotice
typealias RecoveryKind = lab.sf7b.core.RecoveryKind

fun recoveryNoticeText(notice:RecoveryNotice,language:String):String {
    val ru=language=="ru";val name=notice.projectName?:if(ru)"Без названия"else "Untitled"
    return when(notice.kind){
        RecoveryKind.DOCUMENT_BACKUP->if(ru)"Проект «$name» открыт из резервной копии. Последние изменения могли не сохраниться."else "Opened a recovery copy of “$name”. The most recent changes may be missing."
        RecoveryKind.LIBRARY_INDEX->if(ru)"Список проектов восстановлен. Сами проекты не заменялись."else "The project list was recovered. Existing projects were not replaced."
        RecoveryKind.PROJECT_METADATA->if(ru)"Восстановлена карточка проекта «$name». Проверьте работу."else "The project card for “$name” was recovered. Please check the document."
        RecoveryKind.LEGACY_WORKSPACE_FAILED->if(ru)"Прежняя работа не открылась. Её исходные данные сохранены; другие проекты доступны."else "The previous workspace could not be opened. Its original data was retained; other projects remain available."
        RecoveryKind.DELETE_CLEANUP->if(ru)"Проект удалён. Часть временных файлов пока не удалось удалить; другие проекты не затронуты."else "The project was deleted. Some temporary files could not be removed; other projects were not affected."
    }
}
